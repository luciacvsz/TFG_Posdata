package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents whether the analysis result includes a detailed explanation.
 *
 * - [ON] The analysis result includes a detailed human-readable explanation of the verdict.
 * - [OFF] Only the verdict and a short reason are returned.
 */
enum class AppExplanationMode {
    @SerializedName("on")  ON,
    @SerializedName("off") OFF
}