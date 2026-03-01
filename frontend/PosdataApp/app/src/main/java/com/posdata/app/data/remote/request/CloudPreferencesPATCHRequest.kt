package com.posdata.app.data.remote.request

import com.google.gson.annotations.SerializedName
import com.posdata.app.model.*

/**
 * Request body for the cloud preferences PATCH endpoint.
 *
 * Wraps a [PreferencesDTO] object as required by the API contract.
 *
 * @param preferences Preferences to update.
 */
data class CloudPreferencesPATCHRequest(
    @SerializedName("preferences") val preferences: PreferencesDTO
)

/**
 * Data Transfer Object representing the user's application preferences.
 *
 * All fields are optional. Only non-null fields will be serialized and sent to the API,
 * allowing partial updates without overwriting unchanged preferences.
 *
 * @param fontSize New font size, or null to leave it unchanged.
 * @param colorScheme New color scheme, or null to leave it unchanged.
 * @param notificationSound New notification sound setting, or null to leave it unchanged.
 * @param exhaustivity New exhaustivity level, or null to leave it unchanged.
 * @param explanationMode New explanation mode, or null to leave it unchanged.
 */
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