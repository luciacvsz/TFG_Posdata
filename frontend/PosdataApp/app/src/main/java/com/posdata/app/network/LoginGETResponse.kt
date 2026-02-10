package com.posdata.app.network

import com.google.gson.annotations.SerializedName

data class LoginGETResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("session_token") val sessionToken: String,
    @SerializedName("tokens") val tokens: Int
)