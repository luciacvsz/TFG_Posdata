package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserInfo
import com.posdata.app.sms.SMSReceiverManager

class LogoutRepository(
    private val context: Context,
    private val userInfo: UserInfo
) {

    suspend fun performLogout(): Result<String> {
        return try {
            userInfo.clearSession()

            SMSReceiverManager.disableReceiver(context)

            Result.success("Cierre de sesión exitoso")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}