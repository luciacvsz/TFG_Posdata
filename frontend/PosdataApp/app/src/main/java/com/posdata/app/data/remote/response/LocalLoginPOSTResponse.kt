package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the local login POST endpoint.
 *
 * @param success Whether the authentication was successful.
 * @param message Human-readable description of the result.
 * @param userId Unique identifier of the authenticated user.
 */
data class LocalLoginPOSTResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: String,
)