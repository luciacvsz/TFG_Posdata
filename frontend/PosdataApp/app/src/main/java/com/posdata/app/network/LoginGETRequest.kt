package com.posdata.app.network

import com.google.gson.annotations.SerializedName

data class LoginGETRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)