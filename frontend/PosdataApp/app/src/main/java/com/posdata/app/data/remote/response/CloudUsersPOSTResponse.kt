package com.posdata.app.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Response body for the cloud users POST endpoint.
 *
 * Returns the identifier assigned to the newly created user in the remote service.
 *
 * @param userId Unique identifier of the created user.
 */
data class CloudUsersPOSTResponse (
    @SerializedName("user_id") val userId: String
)