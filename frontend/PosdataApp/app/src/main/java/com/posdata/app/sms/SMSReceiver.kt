package com.posdata.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

/**
 * Broadcast receiver that intercepts incoming SMS messages and enqueues
 * them for smishing analysis via [SMishingPostWorker ].
 *
 * Registered in the AndroidManifest but kept disabled by default.
 * It is enabled on login/registration and disabled on logout/account deletion
 * via [SMSReceiverEnabler], ensuring messages are only processed during
 * an active user session.
 *
 * Each SMS message in the received intent is dispatched as an independent
 * [SMishingPostWorker ] job, allowing concurrent analysis of multi-part
 * or batch-received messages.
 */
class SMSReceiver : BroadcastReceiver() {

    /**
     * Called by the system when an SMS broadcast is received.
     *
     * Reconstructs the received SMS and starts its analysis process.
     *
     * @param context Application context provided by the system.
     * @param intent The broadcast intent containing the SMS data.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            if (messages.isEmpty()) return

            val sender = messages[0].displayOriginatingAddress

            val fullMessage = buildString {
                for (sms in messages) {
                    append(sms.messageBody)
                }
            }

            enqueueSMishingAnalysis(context, sender, fullMessage)
        }
    }

    /**
     * Enqueues a one-time [SMishingPostWorker] job for the given SMS message.
     *
     * Passes the sender and message content as input data to the worker.
     * WorkManager guarantees the job will run even if the app is in the background.
     *
     * @param context Application context used to access WorkManager.
     * @param sender Phone number or identifier of the SMS sender.
     * @param message Raw text content of the SMS message.
     */
    private fun enqueueSMishingAnalysis(
        context: Context,
        sender: String,
        message: String
    ) {
        val data = workDataOf(
            "SENDER" to sender,
            "MESSAGE" to message
        )

        val request = OneTimeWorkRequestBuilder<SMishingPostWorker>()
            .setInputData(data)
            .setBackoffCriteria(
                androidx.work.BackoffPolicy.EXPONENTIAL,
                3_000,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )

            .build()

        val uniqueWorkName = "sms_analysis_${System.currentTimeMillis()}_${message.hashCode()}"

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}