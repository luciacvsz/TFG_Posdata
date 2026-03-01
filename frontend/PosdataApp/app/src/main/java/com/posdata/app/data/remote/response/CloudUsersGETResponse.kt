package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.model.TrustedContact

/**
 * Response body for the cloud users GET endpoint.
 *
 * Contains the full profile of the authenticated user as stored in the remote service.
 *
 * @param fullName Full name of the user.
 * @param contact Contact details of the user.
 * @param preferences Application preferences of the user.
 * @param trustedContacts List of trusted contacts associated with the user.
 */
data class CloudUsersGETResponse(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("contact") val contact: Contact,
    @SerializedName("preferences") val preferences: AppPreferences,
    @SerializedName("trusted_contacts") val trustedContacts: List<TrustedContact>
)