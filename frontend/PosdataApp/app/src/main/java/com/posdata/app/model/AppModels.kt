package com.posdata.app.model

import com.google.gson.annotations.SerializedName

data class Contact(
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("email") val email: String
)

enum class AppFontSize {
    @SerializedName("regular")REGULAR,
    @SerializedName("large") LARGE
}

enum class AppNotificationSound {
    @SerializedName("on") ON,
    @SerializedName("off") OFF
}

enum class AppColorScheme {
    @SerializedName("light") LIGHT,
    @SerializedName("dark") DARK,
    @SerializedName("high_contrast") HIGH_CONTRAST,
    @SerializedName("red_green_safe") RED_GREEN_SAFE,
    @SerializedName("blue_yellow_safe") BLUE_YELLOW_SAFE,
    @SerializedName("grayscale") GRAYSCALE
}

enum class AppExhaustivity {
    @SerializedName("regular") REGULAR,
    @SerializedName("enhanced") ENHANCED
}

enum class AppExplanationMode {
    @SerializedName("off") OFF,
    @SerializedName("on") ON
}

data class AppPreferences(
    @SerializedName("font_size") val fontSize: AppFontSize = AppFontSize.REGULAR,
    @SerializedName("notification_sound") val notificationSound: AppNotificationSound = AppNotificationSound.ON,
    @SerializedName("color_scheme") val colorScheme: AppColorScheme = AppColorScheme.LIGHT,
    @SerializedName("exhaustivity") val exhaustivity: AppExhaustivity = AppExhaustivity.REGULAR,
    @SerializedName("explanation_mode") val explanationMode: AppExplanationMode = AppExplanationMode.ON,
)

data class TrustedContact(
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("email") val email: String?
)