package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the local user GET endpoint.
 *
 * @param success Whether the request was successful.
 * @param message Human-readable description of the result.
 */
data class LocalUserGETResponse(
        @SerializedName("success") val success: Boolean,
        @SerializedName("message") val message: String
)