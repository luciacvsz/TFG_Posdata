package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.model.TrustedContact

data class SMSPOSTResponse(
    @SerializedName("execution_id") val executionId: String,
)

