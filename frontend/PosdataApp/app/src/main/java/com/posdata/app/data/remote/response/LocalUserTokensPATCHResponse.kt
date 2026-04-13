package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the local user tokens PATCH endpoint.
 *
 * @param success Whether the token deduction was successfully applied.
 * @param message Human-readable description of the result.
 */
data class LocalUserTokensPATCHResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)