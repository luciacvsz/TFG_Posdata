package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

data class LocalLoginPOSTRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)