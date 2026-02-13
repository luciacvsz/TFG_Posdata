package com.posdata.app.data.repository

import com.posdata.app.data.local.UserInfo
import kotlinx.coroutines.flow.first


class LogoutRepository(
    private val userInfo: UserInfo
) {

    private suspend fun getCurrentUserId(): String? {
        val user = userInfo.userData.first()
        return if (user.isLoggedIn && user.userId.isNotEmpty()) user.userId else null
    }

    suspend fun performLogout(): Result<String> {
        return try {
            userInfo.clearSession()
            Result.success("Cierre de sesión exitoso")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}