package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

data class CloudTrustedContactsPATCHRequest(
    @SerializedName("trusted_contacts")
    val trustedContacts: List<TrustedContactDTO>
)

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