package com.posdata.app.data.repository

import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import kotlinx.coroutines.flow.first

class DeleteAccountRepository(
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserInfo
) {

    private suspend fun getCurrentUserId(): String? {
        val user = userInfo.userData.first()
        return if (user.isLoggedIn && user.userId.isNotEmpty()) user.userId else null
    }

    suspend fun performDeleteAccount(): Result<String> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Usuario no identificado")

            val cloudResponse = cloudApi.deleteUser(userId)
            if (!cloudResponse.isSuccessful)  {
                throw Exception("Error al tratar de eliminar el perfil")
            }

            val localResponse = localApi.deleteUser(userId)

            if (!localResponse.isSuccessful) {
                return Result.failure(Exception("Error al tratar de eliminar el perfil"))
            }

            userInfo.clearSession()

            Result.success("Cuenta eliminada con éxito")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}