package com.posdata.app.network.user

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.model.TrustedContact

data class UserPOSTRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("contact") val contact: Contact,
    @SerializedName("preferences") val preferences: AppPreferences,
    @SerializedName("trusted_contacts") val trustedContacts: List<TrustedContact>
)