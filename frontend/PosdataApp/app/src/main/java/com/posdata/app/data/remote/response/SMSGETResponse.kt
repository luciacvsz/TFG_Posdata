package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.AppColorScheme
import com.posdata.app.model.AppExhaustivity
import com.posdata.app.model.AppExplanationMode
import com.posdata.app.model.AppFontSize
import com.posdata.app.model.AppNotificationSound
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.model.TrustedContact

data class SMSGETResponse(
    @SerializedName("results") val results: ResultsDTO,
)

data class ResultsDTO(
    @SerializedName("user_id") val userId: String,
    @SerializedName("execution_id") val executionId: String,
    @SerializedName("sender") val sender: String,
    @SerializedName("message") val message: String,
    @SerializedName("verdict") val verdict: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("details") val details: String?,
    @SerializedName("processed_at") val processedAt: String
)