package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for the local login POST endpoint.
 *
 * Carries the credentials required to authenticate a user against the local server.
 *
 * @param email Email address of the user.
 * @param password Plain-text password of the user.
 */
data class LocalLoginPOSTRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)