package com.posdata.app.model

import com.google.gson.annotations.SerializedName

/**
 * Represents the notification sound setting of the application.
 *
 * - [ON] Notification always sound.
 * - [OFF] Notification sounds depend on the current phone configuration.
 */
enum class AppNotificationSound {
    @SerializedName("on")  ON,
    @SerializedName("off") OFF
}