package com.posdata.app.sms

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Utility object responsible for enabling and disabling the [SMSReceiver] component.
 *
 * The receiver is enabled on login and registration, and disabled on logout
 * and account deletion, ensuring that SMS messages are only intercepted
 * while the user has an active session.
 */
object SMSReceiverEnabler {

    /**
     * Enables the [SMSReceiver] so that incoming SMS messages are intercepted.
     *
     * @param context Application context used to access the package manager.
     */
    fun enableReceiver(context: Context) {
        setReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    /**
     * Disables the [SMSReceiver] so that incoming SMS messages are no longer intercepted.
     *
     * @param context Application context used to access the package manager.
     */
    fun disableReceiver(context: Context) {
        setReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    /**
     * Updates the enabled state of the [SMSReceiver] component in the package manager.
     *
     * Uses [PackageManager.DONT_KILL_APP] to apply the change without restarting the app.
     *
     * @param context Application context used to access the package manager.
     * @param state The desired component state. Either
     *              [PackageManager.COMPONENT_ENABLED_STATE_ENABLED] or
     *              [PackageManager.COMPONENT_ENABLED_STATE_DISABLED].
     */
    private fun setReceiverState(context: Context, state: Int) {
        val componentName = ComponentName(context, SMSReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            componentName,
            state,
            PackageManager.DONT_KILL_APP
        )
    }
}