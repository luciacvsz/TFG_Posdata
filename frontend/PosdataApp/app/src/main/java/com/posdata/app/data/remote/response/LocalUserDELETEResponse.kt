package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

data class LocalUserDELETEResponse(
        @SerializedName("success") val success: Boolean,
        @SerializedName("message") val message: String
)