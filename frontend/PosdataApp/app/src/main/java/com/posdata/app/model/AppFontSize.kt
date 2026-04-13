package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the font size setting of the application.
 *
 * - [REGULAR] Default font size.
 * - [LARGE] Increased font size for improved readability.
 */
enum class AppFontSize {
    @SerializedName("regular") REGULAR,
    @SerializedName("large")   LARGE
}