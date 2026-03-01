package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for the cloud sms POST endpoint.
 *
 * Carries the data of a received SMS message to be analyzed by the remote service.
 *
 * @param sender Phone number or identifier of the SMS sender.
 * @param message Raw text content of the SMS message.
 */
data class CloudSMSPOSTRequest(
    @SerializedName("sender") val sender: String,
    @SerializedName("message") val message: String
)