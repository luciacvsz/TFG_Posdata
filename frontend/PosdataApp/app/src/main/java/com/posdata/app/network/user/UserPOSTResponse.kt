package com.posdata.app.network.user

import com.google.gson.annotations.SerializedName

data class UserPOSTResponse (
    @SerializedName("user_id") val userId: String
)