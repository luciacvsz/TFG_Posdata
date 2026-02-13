package com.posdata.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class SMSReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress
                val message = sms.messageBody

                analyzeSMishing(context, sender, message)
            }
        }
    }

    private fun analyzeSMishing(context: Context, sender: String, message: String) {
        val data = workDataOf(
            "SENDER" to sender,
            "MESSAGE" to message
        )

        val request = OneTimeWorkRequestBuilder<SMishingAnalysisWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)

    }
}