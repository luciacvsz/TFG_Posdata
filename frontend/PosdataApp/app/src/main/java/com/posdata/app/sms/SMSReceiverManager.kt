package com.posdata.app.sms

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.posdata.app.sms.SMSReceiver

object SMSReceiverManager {
    fun enableReceiver(context: Context) {
        setReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED)
    }

    fun disableReceiver(context: Context) {
        setReceiverState(context, PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
    }

    private fun setReceiverState(context: Context, state: Int) {
        val componentName = ComponentName(context, SMSReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            componentName,
            state,
            PackageManager.DONT_KILL_APP
        )
    }
}