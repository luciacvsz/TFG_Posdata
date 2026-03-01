package com.posdata.app.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

/**
 * Broadcast receiver that intercepts incoming SMS messages and enqueues
 * them for smishing analysis via [SMishingAnalysisWorker].
 *
 * Registered in the AndroidManifest but kept disabled by default.
 * It is enabled on login/registration and disabled on logout/account deletion
 * via [SMSReceiverEnabler], ensuring messages are only processed during
 * an active user session.
 *
 * Each SMS message in the received intent is dispatched as an independent
 * [SMishingAnalysisWorker] job, allowing concurrent analysis of multi-part
 * or batch-received messages.
 */
class SMSReceiver : BroadcastReceiver() {

    /**
     * Called by the system when an SMS broadcast is received.
     *
     * Extracts all SMS messages from the intent and triggers
     * a smishing analysis job for each one.
     *
     * @param context Application context provided by the system.
     * @param intent The broadcast intent containing the SMS data.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            for (sms in messages) {
                val sender = sms.displayOriginatingAddress
                val message = sms.messageBody

                enqueueSMishingAnalysis(context, sender, message)
            }
        }
    }

    /**
     * Enqueues a one-time [SMishingAnalysisWorker] job for the given SMS message.
     *
     * Passes the sender and message content as input data to the worker.
     * WorkManager guarantees the job will run even if the app is in the background.
     *
     * @param context Application context used to access WorkManager.
     * @param sender Phone number or identifier of the SMS sender.
     * @param message Raw text content of the SMS message.
     */
    private fun enqueueSMishingAnalysis(context: Context, sender: String, message: String) {
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