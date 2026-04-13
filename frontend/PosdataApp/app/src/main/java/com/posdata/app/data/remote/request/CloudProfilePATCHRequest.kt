package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for the cloud profile PATCH endpoint.
 *
 * All fields are optional. Only non-null fields will be serialized and sent to the API,
 * allowing partial updates without overwriting unchanged profile data.
 *
 * @param fullName New full name, or null to leave it unchanged.
 * @param phoneNumber New phone number, or null to leave it unchanged.
 * @param email New email address, or null to leave it unchanged.
 */
data class CloudProfilePATCHRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    @SerializedName("email") val email: String? = null
)