package com.posdata.app.network.register

import com.google.gson.annotations.SerializedName

data class CheckEmailPOSTResponse(
        @SerializedName("success") val success: Boolean,
        @SerializedName("message") val message: String
)