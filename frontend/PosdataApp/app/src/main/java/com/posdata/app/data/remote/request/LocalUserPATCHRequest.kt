package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for the local user PATCH endpoint.
 *
 * All fields are optional. Only non-null fields will be serialized and sent to the API,
 * allowing partial updates without overwriting unchanged user data.
 *
 * @param email New email address, or null to leave it unchanged.
 * @param password New plain-text password, or null to leave it unchanged.
 * @param tokens New token balance, or null to leave it unchanged.
 */
data class LocalUserPATCHRequest(
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("tokens") val tokens: Int? = null
)
