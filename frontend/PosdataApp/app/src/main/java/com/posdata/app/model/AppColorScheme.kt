package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the color scheme of the application.
 *
 * Includes standard themes as well as accessibility-focused options
 * for users with color vision deficiencies.
 *
 * - [LIGHT] Default light theme.
 * - [DARK] Dark theme for low-light environments.
 * - [HIGH_CONTRAST] High contrast theme for improved visibility.
 * - [RED_GREEN_SAFE] Adjusted palette for red-green color blindness (deuteranopia/protanopia).
 * - [BLUE_YELLOW_SAFE] Adjusted palette for blue-yellow color blindness (tritanopia).
 * - [GRAYSCALE] Fully desaturated theme.
 */
enum class AppColorScheme {
    @SerializedName("light")            LIGHT,
    @SerializedName("dark")             DARK,
    @SerializedName("high_contrast")    HIGH_CONTRAST,
    @SerializedName("red_green_safe")   RED_GREEN_SAFE,
    @SerializedName("blue_yellow_safe") BLUE_YELLOW_SAFE,
    @SerializedName("grayscale")        GRAYSCALE
}