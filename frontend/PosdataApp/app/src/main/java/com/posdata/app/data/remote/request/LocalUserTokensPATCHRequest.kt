package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Request body for the local user tokens PATCH endpoint.
 *
 * @param operation The billable cloud operation to perform, used by the server
 * to determine the token cost.
 */
data class LocalUserTokensPATCHRequest(
    @SerializedName("operation") val operation: String
)