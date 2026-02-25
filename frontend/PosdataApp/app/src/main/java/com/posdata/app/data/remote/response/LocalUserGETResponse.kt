package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

data class LocalUserGETResponse(
        @SerializedName("success") val success: Boolean,
        @SerializedName("message") val message: String
)