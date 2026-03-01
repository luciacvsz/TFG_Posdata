package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the exhaustivity level of the smishing analysis.
 *
 * - [REGULAR] Only alerts about suspicious or malicious messaages.
 * - [ENHANCED] Alerts about all kinds of received messages.
 */
enum class AppExhaustivity {
    @SerializedName("regular")  REGULAR,
    @SerializedName("enhanced") ENHANCED
}