package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the cloud sms GET endpoint.
 *
 * Contains the full analysis result of a previously submitted SMS message.
 *
 * @param results Analysis result details.
 */
data class CloudSMSGETResponse(
    @SerializedName("results") val results: ResultsDTO,
)

/**
 * Data Transfer Object containing the analysis result of an SMS message.
 *
 * @param userId Identifier of the user who submitted the SMS.
 * @param executionId Unique identifier of the analysis execution.
 * @param sender Phone number or identifier of the SMS sender.
 * @param message Raw text content of the analyzed SMS.
 * @param verdict Analysis verdict (e.g. "safe", "suspicious").
 * @param reason Brief explanation of the verdict.
 * @param details Extended analysis details, or null if not available.
 * @param processedAt ISO 8601 timestamp indicating when the analysis was completed.
 */
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