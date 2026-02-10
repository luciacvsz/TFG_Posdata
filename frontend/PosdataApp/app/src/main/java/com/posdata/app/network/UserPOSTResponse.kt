package com.posdata.app.network

import com.google.gson.annotations.SerializedName

data class UserPOSTResponse (
    @SerializedName("user_id") val UserId: String
)