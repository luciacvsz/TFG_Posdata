package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

data class CloudSMSPOSTRequest(
    @SerializedName("sender") val sender: String,
    @SerializedName("message") val message: String
)