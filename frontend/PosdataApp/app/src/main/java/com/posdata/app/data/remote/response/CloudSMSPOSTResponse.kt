package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

data class CloudSMSPOSTResponse(
    @SerializedName("execution_id") val executionId: String,
)

