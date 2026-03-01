package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the local user POST endpoint.
 *
 * @param success Whether the user was successfully created.
 * @param message Human-readable description of the result.
 * @param tokens Initial token balance assigned to the newly created user.
 */
data class LocalUserPOSTResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("tokens") val tokens: Int
)