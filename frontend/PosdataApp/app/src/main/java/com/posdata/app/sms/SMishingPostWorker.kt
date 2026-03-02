package com.posdata.app.sms

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.remote.request.CloudSMSPOSTRequest
import com.posdata.app.data.remote.request.LocalUserPATCHRequest
import com.posdata.app.data.repository.CloudCosts
import com.posdata.app.data.repository.TokenConsumptionRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Background worker responsible for submitting an incoming SMS to the cloud
 * smishing analysis service and enqueuing the follow-up polling worker.
 *
 * Triggered by [SMSReceiver] via WorkManager whenever an SMS is received.
 * Runs as a [CoroutineWorker] to support suspend functions.
 *
 * ## Analysis flow
 * 1. Verifies the user is authenticated and has sufficient tokens.
 * 2. Submits the SMS to the cloud via POST and retrieves the execution ID.
 * 3. Enqueues [SMishingPollingWorker] with the execution ID to poll for results.
 *
 * Returns [Result.retry] on transient failures (network errors) and
 * [Result.failure] on permanent failures (insufficient tokens, missing input data,
 * user not authenticated).
 */
class SMishingPostWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val localApi = RetrofitClient.localInstance
    private val cloudApi = RetrofitClient.cloudInstance
    private val userInfo = UserDataStore(applicationContext)
    private val tokenConsumptionRepository = TokenConsumptionRepository(userInfo)

    /**
     * Executes the SMS submission flow.
     *
     * Expects the following input data keys:
     * - `SENDER`: phone number or identifier of the SMS sender.
     * - `MESSAGE`: raw text content of the SMS message.
     *
     * On success, enqueues a [SMishingPollingWorker] identified by
     * `sms_polling_<executionId>` to handle result retrieval.
     *
     * @return [Result.success] if the SMS was submitted and the polling worker was enqueued;
     *         [Result.retry] if a transient network error occurred;
     *         [Result.failure] if a permanent error occurred (unauthenticated, insufficient
     *         tokens, or missing input data).
     */
    override suspend fun doWork(): Result {

        val sender = inputData.getString("SENDER") ?: return Result.failure()
        val message = inputData.getString("MESSAGE") ?: return Result.failure()

        val userData = userInfo.userData.first()
        if (!userData.isLoggedIn) return Result.failure()

        val tokenResult =
            tokenConsumptionRepository.haveEnoughTokens(CloudCosts.POST_SMS)

        if (tokenResult.isFailure) {
            return Result.failure()
        }

        val tokenPatchResponse = localApi.patchUser(
            userId  = userData.userId,
            request = LocalUserPATCHRequest(tokens = userInfo.userData.first().tokens)
        )
        if (!tokenPatchResponse.isSuccessful || tokenPatchResponse.body() == null) {
            return Result.failure()
        }
        val postResponse = cloudApi.postSMS(
            userId = userData.userId,
            request = CloudSMSPOSTRequest(sender, message)
        )

        if (!postResponse.isSuccessful || postResponse.body() == null) {
            return Result.retry()
        }

        val executionId = postResponse.body()!!.executionId

        enqueuePollingWorker(
            applicationContext,
            sender,
            message,
            executionId
        )

        return Result.success()
    }

    /**
     * Enqueues a [SMishingPollingWorker] to poll for the analysis result.
     *
     * Uses [ExistingWorkPolicy.KEEP] with a unique name derived from [executionId]
     * to avoid duplicate polling workers for the same request.
     * Applies an exponential backoff policy with an initial delay of 10 seconds.
     *
     * @param context Application context used to obtain the [WorkManager] instance.
     * @param sender Phone number or identifier of the SMS sender.
     * @param message Raw text content of the SMS message.
     * @param executionId Cloud execution ID returned by the POST endpoint.
     */
    private fun enqueuePollingWorker(
        context: Context,
        sender: String,
        message: String,
        executionId: String
    ) {

        val data = workDataOf(
            "SENDER" to sender,
            "MESSAGE" to message,
            "EXECUTION_ID" to executionId
        )

        val request =
            OneTimeWorkRequestBuilder<SMishingPollingWorker>()
                .setInputData(data)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10_000,
                    TimeUnit.MILLISECONDS
                )
                .build()

        val uniqueName = "sms_polling_$executionId"

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}