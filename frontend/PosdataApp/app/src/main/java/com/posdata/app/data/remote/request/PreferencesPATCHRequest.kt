package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.*

data class PreferencesPATCHRequest(
    @SerializedName("preferences") val preferences: PreferencesDTO
)

data class PreferencesDTO(
    @SerializedName("font_size")
    val fontSize: AppFontSize? = null,

    @SerializedName("color_scheme")
    val colorScheme: AppColorScheme? = null,

    @SerializedName("notification_sound")
    val notificationSound: AppNotificationSound? = null,

    @SerializedName("exhaustivity")
    val exhaustivity: AppExhaustivity? = null,

    @SerializedName("explanation_mode")
    val explanationMode: AppExplanationMode? = null
)