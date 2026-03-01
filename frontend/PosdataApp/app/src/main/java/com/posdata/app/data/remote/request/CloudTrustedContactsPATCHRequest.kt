package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for the cloud trusted-contacts PATCH endpoint.
 *
 * Replaces the full list of trusted contacts stored on the server.
 *
 * @param trustedContacts New list of trusted contacts.
 */
data class CloudTrustedContactsPATCHRequest(
    @SerializedName("trusted_contacts")
    val trustedContacts: List<TrustedContactDTO>
)

/**
 * Data Transfer Object representing a single trusted contact.
 *
 * @param name Full name of the contact.
 * @param role Role or relationship of the contact (e.g. "son", "neighbour").
 * @param phoneNumber Phone number of the contact, or null if not available.
 * @param email Email address of the contact, or null if not available.
 */
data class TrustedContactDTO(
    @SerializedName("name")
    val name: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("email")
    val email: String? = null
)