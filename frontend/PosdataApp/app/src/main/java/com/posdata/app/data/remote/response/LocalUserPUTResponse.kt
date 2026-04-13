package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the local user POST endpoint.
 *
 * @param success Whether the user was successfully created.
 * @param message Human-readable description of the result.
 */
data class LocalUserPUTResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
)