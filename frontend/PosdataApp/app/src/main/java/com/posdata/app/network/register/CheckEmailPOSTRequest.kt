package com.posdata.app.network.register

import com.google.gson.annotations.SerializedName

data class CheckEmailPOSTRequest(
    @SerializedName("email") val email: String,
)