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
    @SerializedName("standard") STANDARD,
    @SerializedName("high_contrast") HIGH_CONTRAST,
    @SerializedName("protanopia") PROTANOPIA,
    @SerializedName("deuteranopia") DEUTERANOPIA,
    @SerializedName("tritanopia") TRITANOPIA,
    @SerializedName("achromatopsia") ACHROMATOPSIA
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
    @SerializedName("color_scheme") val colorScheme: AppColorScheme = AppColorScheme.STANDARD,
    @SerializedName("exhaustivity") val exhaustivity: AppExhaustivity = AppExhaustivity.REGULAR,
    @SerializedName("explanation_mode") val explanationMode: AppExplanationMode = AppExplanationMode.ON,
)

data class TrustedContact(
    @SerializedName("name") val name: String,
    @SerializedName("role") val role: String,
    @SerializedName("phone_number") val phoneNumber: String?,
    @SerializedName("email") val email: String?
)