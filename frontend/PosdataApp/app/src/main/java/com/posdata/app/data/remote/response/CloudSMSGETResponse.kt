package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

data class CloudSMSGETResponse(
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