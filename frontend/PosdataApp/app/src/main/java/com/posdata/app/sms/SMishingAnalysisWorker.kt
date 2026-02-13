package com.posdata.app.sms;

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.remote.request.SMSPOSTRequest
import com.posdata.app.data.remote.response.ResultsDTO
import com.posdata.app.data.remote.response.SMSGETResponse
import com.posdata.app.model.AppExhaustivity
import com.posdata.app.model.AppExplanationMode
import com.posdata.app.model.AppNotificationSound
import com.posdata.app.model.AppPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

class SMishingAnalysisWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val cloudApi = RetrofitClient.cloudInstance

    override suspend fun doWork(): Result {
        val userInfo = UserInfo(applicationContext)

        val userData = userInfo.userData.first()

        if (!userData.isLoggedIn || userData.sessionToken.isEmpty()) {
            return Result.failure()
        }

        val sender = inputData.getString("SENDER") ?: return Result.failure()
        val message = inputData.getString("MESSAGE") ?: return Result.failure()

        return try {
            val postResponse = cloudApi.postSMS(userData.userId, SMSPOSTRequest(sender, message))
            if(!postResponse.isSuccessful || postResponse.body() == null) {
                return Result.retry()
            }

            val executionId = postResponse.body()!!.executionId

            var checkResult: SMSGETResponse? = null
            var attempts = 0
            val maxAttempts = 5

            while(attempts < maxAttempts) {
                delay(2000)

                val getResponse = cloudApi.getSMS(userData.userId, executionId)
                if(getResponse.isSuccessful && getResponse.body() != null) {
                    checkResult = getResponse.body()
                    break
                }

                attempts++
            }

            if (checkResult == null){
                return Result.retry()
            }

            handleNotification(userData.preferences, sender, checkResult)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun handleNotification(
        preferences: AppPreferences,
        sender: String,
        results: SMSGETResponse
    ) {
        val dto: ResultsDTO = results.results
        val isSMishing = (dto.verdict == "MALICIOUS" || dto.verdict == "SUSPICIOUS")

        val shouldNotify = when (preferences.exhaustivity) {
            AppExhaustivity.REGULAR -> isSMishing
            AppExhaustivity.ENHANCED -> true
        }

        if (shouldNotify) {
            showNotification(sender, preferences.notificationSound, preferences.explanationMode, dto)
        }
    }

    private fun showNotification(
        sender: String,
        notificationSound: AppNotificationSound,
        explanationMode: AppExplanationMode,
        results: ResultsDTO
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "posdata_analysis_channel"

        val title = if (results.verdict == "malicious") "⚠️ ¡Amenaza Detectada!"
        else if (results.verdict == "suspicious") "⚠️ Posible Mensaje Malicioso"
        else "✅ Mensaje Seguro"

        val icon = if (results.verdict == "malicious" || results.verdict == "suspicious")
            android.R.drawable.stat_sys_warning else android.R.drawable.ic_dialog_info

        val colorAccent = if (results.verdict == "malicious") Color.RED
        else if (results.verdict == "suspicious") Color.YELLOW
        else Color.GREEN

        val shortText = results.reason
        val longText = if (explanationMode == AppExplanationMode.ON && !results.details.isNullOrEmpty()) {
            "${results.reason}\n\n🔍 Detalles técnicos:\n${results.details}"
        } else {
            results.reason
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

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(shortText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(longText)) // Texto expandible
            .setColor(colorAccent)
            .setAutoCancel(true)
            .setPriority(if (notificationSound == AppNotificationSound.ON) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_LOW)

        if (notificationSound == AppNotificationSound.ON) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        } else {
            builder.setSound(null)
            builder.setVibrate(longArrayOf(0L))
        }

        if (!notificationManager.areNotificationsEnabled()) {
            Log.e("NotifCheck", "¡ERROR! Las notificaciones están desactivadas para la app.")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(channelId)
            if (channel == null) {
                Log.e("NotifCheck", "¡ERROR! El canal '$channelId' NO EXISTE. Debes crearlo antes.")
            } else if (channel.importance == NotificationManager.IMPORTANCE_NONE) {
                Log.e("NotifCheck", "¡ERROR! El usuario ha bloqueado este canal específico.")
            } else {
                Log.i("NotifCheck", "Canal correcto. Importancia: ${channel.importance}")
            }
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
