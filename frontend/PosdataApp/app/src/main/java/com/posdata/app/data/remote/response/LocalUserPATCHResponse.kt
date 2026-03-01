package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the local user PATCH endpoint.
 *
 * @param success Whether the update was successfully applied.
 * @param message Human-readable description of the result.
 */
data class LocalUserPATCHResponse(
        @SerializedName("success") val success: Boolean,
        @SerializedName("message") val message: String
)