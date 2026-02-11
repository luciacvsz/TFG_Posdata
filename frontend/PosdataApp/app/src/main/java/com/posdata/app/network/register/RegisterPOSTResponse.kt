package com.posdata.app.network.register

import com.google.gson.annotations.SerializedName

data class RegisterPOSTResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("session_token") val sessionToken: String,
    @SerializedName("tokens") val tokens: Int
)