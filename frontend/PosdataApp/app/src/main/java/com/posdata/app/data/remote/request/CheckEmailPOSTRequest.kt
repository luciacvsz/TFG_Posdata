package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

data class CheckEmailPOSTRequest(
    @SerializedName("email") val email: String,
)