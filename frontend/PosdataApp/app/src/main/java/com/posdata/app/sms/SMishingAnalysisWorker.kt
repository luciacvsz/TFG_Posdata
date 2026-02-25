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
import retrofit2.Retrofit

class SMishingAnalysisWorker(
        context: Context,
        workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val cloudApi = RetrofitClient.cloudInstance
    private val localApi = RetrofitClient.localInstance
    private val tokenConsumptionRepository = TokenConsumptionRepository(
        UserInfo(applicationContext))

    override suspend fun doWork(): Result {
        val userInfo = UserInfo(applicationContext)

        val userData = userInfo.userData.first()

        if (!userData.isLoggedIn ) {
            return Result.failure()
        }

        val sender = inputData.getString("SENDER") ?: return Result.failure()
        val message = inputData.getString("MESSAGE") ?: return Result.failure()

        return try {
            val tokenResult1 = tokenConsumptionRepository.haveEnoughTokens(CloudCosts.POST_SMS)
            if (tokenResult1.isFailure) {
                Log.e("AnalysisWorker", "Tokens insuficientes para analizar SMS")
                return Result.failure()
            }

            val localResponse1 = localApi.patchUser(userId = userData.userId,
                LocalUserPATCHRequest(null, null, CloudCosts.POST_SMS)
            )
            if(!localResponse1.isSuccessful || localResponse1.body() == null) {
                Log.e("AnalysisWorker", "Error inesperado actualizando tokens en la base de datos local. El SMS no se pudo analizar.")
                return Result.failure()
            }

            val postResponse = cloudApi.postSMS(userData.userId, CloudSMSPOSTRequest(sender, message))
            if(!postResponse.isSuccessful || postResponse.body() == null) {
                return Result.retry()
            }

            val executionId = postResponse.body()!!.executionId

            var checkResult: CloudSMSGETResponse? = null
            var attempts = 0
            val maxAttempts = 5

            while(attempts < maxAttempts) {
                delay(2000)

                val tokenResult2 = tokenConsumptionRepository.haveEnoughTokens(CloudCosts.GET_SMS)
                if (tokenResult2.isFailure) {
                    Log.e("AnalysisWorker", "Tokens insuficientes para analizar SMS")
                    return Result.failure()
                }

                val localResponse2 = localApi.patchUser(userId = userData.userId,
                    LocalUserPATCHRequest(null, null, CloudCosts.GET_SMS)
                )
                if(!localResponse2.isSuccessful || localResponse2.body() == null) {
                    Log.e("AnalysisWorker", "Error inesperado actualizando tokens en la base de datos local. El SMS no se pudo analizar.")
                    return Result.failure()
                }

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
        results: CloudSMSGETResponse
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
