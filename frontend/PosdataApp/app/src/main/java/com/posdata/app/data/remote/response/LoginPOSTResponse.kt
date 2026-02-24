package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

data class LoginPOSTResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("tokens") val tokens: Int
)