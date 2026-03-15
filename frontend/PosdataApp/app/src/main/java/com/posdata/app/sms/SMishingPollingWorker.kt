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
import com.posdata.app.data.remote.response.CloudSMSGETResponse
import com.posdata.app.data.remote.response.ResultsDTO
import com.posdata.app.model.AppExhaustivity
import com.posdata.app.model.AppExplanationMode
import com.posdata.app.model.AppNotificationSound
import com.posdata.app.model.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Background worker responsible for polling the cloud smishing analysis service
 * for a previously submitted SMS result and displaying a notification with the verdict.
 *
 * Enqueued by [SMishingPostWorker] after a successful POST. Runs as a [CoroutineWorker]
 * to support suspend functions and coroutine-based delays.
 *
 * ## Analysis flow
 * 1. Validates required input data (sender, message, execution ID).
 * 2. Waits 2 seconds before polling to allow the cloud service to process the request.
 * 3. Polls the cloud via GET using the provided execution ID.
 * 4. Displays a notification based on the verdict and the user's preferences.
 *
 * Retries up to [_maxAttempts] times using the backoff policy configured by
 * [SMishingPostWorker]. Returns [Result.failure] if the attempt limit is exceeded
 * or required input data is missing.
 */
class SMishingPollingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    private val _maxAttempts = 5
    private val cloudApi = RetrofitClient.cloudInstance
    private val userInfo = UserDataStore(applicationContext)

    /**
     * Executes the polling and notification flow.
     *
     * Expects the following input data keys:
     * - `SENDER`: phone number or identifier of the SMS sender.
     * - `MESSAGE`: raw text content of the SMS message.
     * - `EXECUTION_ID`: cloud execution ID obtained from [SMishingPostWorker].
     *
     * @return [Result.success] if the analysis result was retrieved and the notification
     *         was handled;
     *         [Result.retry] if the cloud response was unsuccessful or unavailable;
     *         [Result.failure] if the maximum attempt count was reached or required
     *         input data is missing.
     */
    override suspend fun doWork(): Result {

        if (runAttemptCount >= _maxAttempts) {
            return Result.failure()
        }

        val sender = inputData.getString("SENDER") ?: return Result.failure()
        val message = inputData.getString("MESSAGE") ?: return Result.failure()
        val executionId = inputData.getString("EXECUTION_ID") ?: return Result.failure()

        val userData = userInfo.userData.first()

        delay(2000)

        val getResponse =
            cloudApi.getSMS(userData.userId, executionId)
        Log.d("SMishingPolling", "Raw body: ${getResponse.body()}")

        if (!getResponse.isSuccessful || getResponse.body() == null) {
            return Result.retry()
        }

        handleNotification(
            userData.preferences,
            sender,
            message,
            getResponse.body()!!
        )

        return Result.success()
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
        val isThreat = dto.verdict == "malicious" || dto.verdict == "suspicious"

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
            "malicious"  -> "⚠️ ¡Amenaza Detectada!"
            "suspicious" -> "⚠️ Mensaje Sospechoso"
            else         -> "✅ Mensaje Seguro"
        }

        val icon = when (results.verdict) {
            "malicious", "suspicious" -> android.R.drawable.stat_sys_warning
            else                      -> android.R.drawable.ic_dialog_info
        }

        val colorAccent = when (results.verdict) {
            "malicious"  -> Color.RED
            "suspicious" -> Color.YELLOW
            else         -> Color.GREEN
        }

        val isThreat = results.verdict == "malicious" || results.verdict == "SUSPICIOUS"

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
            val importance = NotificationManager.IMPORTANCE_HIGH

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
                NotificationCompat.PRIORITY_DEFAULT
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