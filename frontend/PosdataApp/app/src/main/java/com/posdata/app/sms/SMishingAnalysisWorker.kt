package com.posdata.app.sms

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.remote.request.CloudSMSPOSTRequest
import com.posdata.app.data.remote.request.LocalUserPATCHRequest
import com.posdata.app.data.remote.response.ResultsDTO
import com.posdata.app.data.remote.response.CloudSMSGETResponse
import com.posdata.app.data.repository.CloudCosts
import com.posdata.app.data.repository.TokenConsumptionRepository
import com.posdata.app.model.AppExhaustivity
import com.posdata.app.model.AppExplanationMode
import com.posdata.app.model.AppNotificationSound
import com.posdata.app.model.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Background worker responsible for submitting an incoming SMS to the cloud
 * smishing analysis service and displaying a notification with the result.
 *
 * Triggered by [SMSReceiver] via WorkManager whenever an SMS is received.
 * Runs as a [CoroutineWorker] to support suspend functions and coroutine-based delays.
 *
 * ## Analysis flow
 * 1. Verifies the user is authenticated and has sufficient tokens.
 * 2. Deducts the [CloudCosts.POST_SMS] token cost from the local database.
 * 3. Submits the SMS to the cloud via POST and retrieves the execution ID.
 * 4. Polls the cloud via GET until the result is available or the attempt limit is reached.
 * 5. Displays a notification based on the verdict and the user's preferences.
 *
 * Returns [Result.retry] on transient failures (network errors, cloud not ready)
 * and [Result.failure] on permanent failures (insufficient tokens, missing input data).
 */
class SMishingAnalysisWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val cloudApi = RetrofitClient.cloudInstance
    private val localApi = RetrofitClient.localInstance
    private val userInfo = UserDataStore(applicationContext)
    private val tokenConsumptionRepository = TokenConsumptionRepository(userInfo)

    /**
     * Executes the smishing analysis flow.
     *
     * Expects the following input data keys:
     * - `SENDER`: phone number or identifier of the SMS sender.
     * - `MESSAGE`: raw text content of the SMS message.
     *
     * @return [Result.success] if the analysis completed and the notification was shown;
     *         [Result.retry] if a transient error occurred;
     *         [Result.failure] if a permanent error occurred.
     */
    override suspend fun doWork(): Result {
        val userData = userInfo.userData.first()

        if (!userData.isLoggedIn ) {
            return Result.failure()
        }

        val sender = inputData.getString("SENDER") ?: return Result.failure()
        val message = inputData.getString("MESSAGE") ?: return Result.failure()

        return try {
            val tokenResult1 = tokenConsumptionRepository.haveEnoughTokens(CloudCosts.POST_SMS)
            if (tokenResult1.isFailure) {
                Log.e("SMishingWorker", "Insufficient tokens to analyze SMS")
                return Result.failure()
            }

            val tokenPatchResponse = localApi.patchUser(
                userId  = userData.userId,
                request = LocalUserPATCHRequest(tokens = userData.tokens)
            )
            if (!tokenPatchResponse.isSuccessful || tokenPatchResponse.body() == null) {
                Log.e("SMishingWorker", "Failed to update token balance in local database")
                return Result.failure()
            }

            val postResponse = cloudApi.postSMS(
                userId  = userData.userId,
                request = CloudSMSPOSTRequest(sender, message)
            )
            if (!postResponse.isSuccessful || postResponse.body() == null) {
                return Result.retry()
            }

            val executionId = postResponse.body()!!.executionId

            var analysisResult: CloudSMSGETResponse? = null
            var attempts = 0
            val maxAttempts = 5

            while(attempts < maxAttempts) {
                delay(2000)

                val getResponse = cloudApi.getSMS(userData.userId, executionId)
                if (getResponse.isSuccessful && getResponse.body() != null) {
                    analysisResult = getResponse.body()
                    break
                }

                attempts++
            }

            if (analysisResult == null){
                return Result.retry()
            }

            handleNotification(userData.preferences, sender, message, analysisResult)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    /**
     * Determines whether a notification should be shown based on the user's
     * exhaustivity preference and the analysis verdict.
     *
     * - [AppExhaustivity.REGULAR]: only notifies on MALICIOUS or SUSPICIOUS verdicts.
     * - [AppExhaustivity.ENHANCED]: always notifies, including SAFE verdicts.
     *
     * @param preferences Current user preferences.
     * @param sender Phone number or identifier of the SMS sender.
     * @param message SMS received by the user.
     * @param results Full analysis response from the cloud.
     */
    private fun handleNotification(
        preferences: AppPreferences,
        sender: String,
        message: String,
        results: CloudSMSGETResponse
    ) {
        val dto: ResultsDTO = results.results
        val isThreat = dto.verdict == "MALICIOUS" || dto.verdict == "SUSPICIOUS"

        val shouldNotify = when (preferences.exhaustivity) {
            AppExhaustivity.REGULAR  -> isThreat
            AppExhaustivity.ENHANCED -> true
        }

        if (shouldNotify) {
            showNotification(
                sender = sender,
                message = message,
                notificationSound = preferences.notificationSound,
                explanationMode = preferences.explanationMode,
                results = dto
            )
        }
    }

    /**
     * Builds and displays a system notification with the analysis result.
     *
     * The notification title, icon, and accent color reflect the verdict severity.
     * Sound and vibration are controlled by the user's [notificationSound] preference.
     * Extended details are shown in the expanded notification view if
     * [explanationMode] is [AppExplanationMode.ON] and details are available.
     *
     * @param sender Phone number or identifier of the SMS sender.
     * @param message SMS received by the user.
     * @param notificationSound Whether sound and vibration are enabled.
     * @param explanationMode Whether extended analysis details should be shown.
     * @param results Analysis result DTO containing verdict, reason, and details.
     */
    private fun showNotification(
        sender: String,
        message: String,
        notificationSound: AppNotificationSound,
        explanationMode: AppExplanationMode,
        results: ResultsDTO
    ) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "posdata_analysis_channel"

        val title = when (results.verdict) {
            "MALICIOUS"  -> "⚠️ ¡Amenaza Detectada!"
            "SUSPICIOUS" -> "⚠️ Mensaje Sospechoso"
            else         -> "✅ Mensaje Seguro"
        }

        val icon = when (results.verdict) {
            "MALICIOUS", "SUSPICIOUS" -> android.R.drawable.stat_sys_warning
            else                      -> android.R.drawable.ic_dialog_info
        }

        val colorAccent = when (results.verdict) {
            "MALICIOUS"  -> Color.RED
            "SUSPICIOUS" -> Color.YELLOW
            else         -> Color.GREEN
        }

        val isThreat = results.verdict == "MALICIOUS" || results.verdict == "SUSPICIOUS"

        val shortText = "De: $sender — ${results.reason}"

        val longText = buildString {
            append("De: $sender\n")
            append(results.reason)

            if (isThreat) {
                append("\n\nMensaje Original:\n\"$message\"")
            }

            if (explanationMode == AppExplanationMode.ON && !results.details.isNullOrEmpty()) {
                append("\n\n🔍 Detalles:\n${results.details}")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (notificationSound == AppNotificationSound.ON)
                NotificationManager.IMPORTANCE_HIGH
            else
                NotificationManager.IMPORTANCE_LOW

            val channel = NotificationChannel(channelId, "Análisis de SMS", importance).apply {
                description = "Notificaciones de seguridad de mensajes"
                enableLights(true)
                lightColor = colorAccent
                enableVibration(notificationSound == AppNotificationSound.ON)

                if (notificationSound == AppNotificationSound.OFF) {
                    setSound(null, null)
                }
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(longText))
            .setColor(colorAccent)
            .setAutoCancel(true)
            .setPriority(
                if (notificationSound == AppNotificationSound.ON)
                    NotificationCompat.PRIORITY_HIGH
                else
                    NotificationCompat.PRIORITY_LOW
            )
            .apply {
                if (notificationSound == AppNotificationSound.ON) {
                    setDefaults(NotificationCompat.DEFAULT_ALL)
                } else {
                    setSound(null)
                    setVibrate(longArrayOf(0L))
                }
            }
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
