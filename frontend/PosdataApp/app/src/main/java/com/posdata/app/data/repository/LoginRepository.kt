package com.posdata.app.data.repository

import com.posdata.app.data.local.UserInfo
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.data.remote.ApiService
import com.posdata.app.data.remote.request.LoginPOSTRequest

class LoginRepository(
    private val localApi: ApiService,
    private val cloudApi: ApiService,
    private val userInfo: UserInfo
) {
    suspend fun performLoginAndSync(email: String, password: String): Result<String> {
        return try {
            val localResp = localApi.postLogin(LoginPOSTRequest(email, password))
            val localData = localResp.body()

            if (!localResp.isSuccessful || localData == null || !localData.success) {
                return Result.failure(Exception(localData?.message ?: "Error en credenciales"))
            }

            val userId = localData.userId
                ?: return Result.failure(Exception("Login correcto pero sin ID de usuario"))

            val sessionToken = localData.sessionToken
            val tokens = localData.tokens

            val cloudData = try {
                val cloudResp = cloudApi.getUser(userId)
                if (cloudResp.isSuccessful) cloudResp.body() else null
            } catch (e: Exception) {
                return Result.failure(Exception("Ha ocurrido un error accediendo a los datos del usuario en el cloud"))
            }

            val fullName = cloudData?.fullName ?: "Usuario"
            val contact = cloudData?.contact ?: Contact(
                phoneNumber = "",
                email = email
            )
            val preferences = cloudData?.preferences  ?: AppPreferences()
            val trustedContacts = cloudData?.trustedContacts ?: emptyList()

            userInfo.saveUserSession(
                userId = userId,
                sessionToken = sessionToken,
                tokens = tokens,
                fullName = fullName,
                contact = contact,
                preferences = preferences,
                trustedContacts = trustedContacts
            )

            Result.success("Login completado")

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}