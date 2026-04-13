package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the contact details of a user.
 *
 * @param phoneNumber Phone number of the user.
 * @param email Email address of the user.
 */
data class Contact(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("email")        val email: String
)