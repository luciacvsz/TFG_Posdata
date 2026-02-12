package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

data class UserPOSTResponse (
    @SerializedName("user_id") val userId: String
)