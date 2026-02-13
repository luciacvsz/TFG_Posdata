package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.model.TrustedContact

data class SMSPOSTRequest(
    @SerializedName("sender") val sender: String,
    @SerializedName("message") val message: String
)