package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the local user GET endpoint.
 *
 * @param success Whether the user exists in the local database.
 * @param active Whether the user account is currently active. Null if the user does not exist.
 * @param userId Unique identifier of the user, or null if the user does not exist.
 * @param message Human-readable description of the result.
 */
data class LocalUserGETResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("active") val active: Boolean?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("message") val message: String
)