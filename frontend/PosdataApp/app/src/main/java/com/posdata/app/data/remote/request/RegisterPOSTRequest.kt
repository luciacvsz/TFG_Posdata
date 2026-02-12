package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

data class RegisterPOSTRequest(
    @SerializedName("user_id") val userId: String?,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)