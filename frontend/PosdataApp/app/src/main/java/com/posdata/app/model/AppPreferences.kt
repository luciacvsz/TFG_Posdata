package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the full set of user-configurable application preferences.
 *
 * All fields have default values so that a valid [AppPreferences] instance
 * can be created without arguments.
 *
 * @param fontSize Font size preference. Defaults to [AppFontSize.REGULAR].
 * @param notificationSound Notification sound preference. Defaults to [AppNotificationSound.ON].
 * @param colorScheme Color scheme preference. Defaults to [AppColorScheme.LIGHT].
 * @param exhaustivity Analysis exhaustivity level. Defaults to [AppExhaustivity.REGULAR].
 * @param explanationMode Whether analysis results include explanations. Defaults to [AppExplanationMode.ON].
 */
data class AppPreferences(
    @SerializedName("font_size")          val fontSize: AppFontSize               = AppFontSize.REGULAR,
    @SerializedName("notification_sound") val notificationSound: AppNotificationSound = AppNotificationSound.ON,
    @SerializedName("color_scheme")       val colorScheme: AppColorScheme         = AppColorScheme.LIGHT,
    @SerializedName("exhaustivity")       val exhaustivity: AppExhaustivity       = AppExhaustivity.REGULAR,
    @SerializedName("explanation_mode")   val explanationMode: AppExplanationMode = AppExplanationMode.ON
)