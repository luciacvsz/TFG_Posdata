package com.posdata.app.network.login

import com.google.gson.annotations.SerializedName

data class LoginPOSTRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)