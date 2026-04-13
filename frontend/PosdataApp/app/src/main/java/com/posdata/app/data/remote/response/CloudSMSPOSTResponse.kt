package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the cloud sms POST endpoint.
 *
 * The analysis is performed asynchronously. This response provides the [executionId]
 * that can be used to poll the cloud sms GET endpoint for the final result.
 *
 * @param executionId Unique identifier of the analysis execution.
 */
data class CloudSMSPOSTResponse(
    @SerializedName("execution_id") val executionId: String,
)

