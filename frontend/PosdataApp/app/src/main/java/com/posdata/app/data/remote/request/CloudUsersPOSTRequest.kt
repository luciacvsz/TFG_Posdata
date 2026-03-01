package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for the cloud users POST endpoint.
 *
 * Carries the minimum required data to register a new user in the remote service.
 *
 * @param fullName Full name of the user.
 * @param phoneNumber Phone number of the user.
 * @param email Email address of the user.
 */
data class CloudUsersPOSTRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("email") val email: String
)