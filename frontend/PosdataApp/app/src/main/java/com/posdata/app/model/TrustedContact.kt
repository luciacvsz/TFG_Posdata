package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents a trusted contact associated with the user.
 *
 * Trusted contacts are people the user designates to be notified
 * or consulted in the event of a smishing threat detection.
 *
 * @param name Full name of the contact.
 * @param role Role or relationship of the contact (e.g. "family", "friend").
 * @param phoneNumber Phone number of the contact, or null if not provided.
 * @param email Email address of the contact, or null if not provided.
 */
data class TrustedContact(
    @SerializedName("name")         val name: String,
    @SerializedName("role")         val role: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("email")        val email: String?
)