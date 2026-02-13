package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

data class CloudProfilePATCHRequest(
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("email") val email: String?
)