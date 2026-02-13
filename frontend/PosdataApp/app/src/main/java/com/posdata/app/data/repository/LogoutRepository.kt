package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserInfo
import com.posdata.app.sms.SMSReceiverManager
import kotlinx.coroutines.flow.first


class LogoutRepository(
    private val context: Context,
    private val userInfo: UserInfo
) {

    private suspend fun getCurrentUserId(): String? {
        val user = userInfo.userData.first()
        return if (user.isLoggedIn && user.userId.isNotEmpty()) user.userId else null
    }

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