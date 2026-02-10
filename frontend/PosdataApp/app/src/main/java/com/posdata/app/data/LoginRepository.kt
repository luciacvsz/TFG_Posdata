package com.posdata.app.data

import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.network.ApiService
import com.posdata.app.network.LoginGETRequest

class LoginRepository(
    private val localApi: ApiService,
    private val cloudApi: ApiService,
    private val userInfo: UserInfo
) {

    suspend fun performLoginAndSync(email: String, password: String): Result<String> {
        return try {
            val loginResp = localApi.login(LoginGETRequest(email, password))
            val loginData = loginResp.body()

            if (!loginResp.isSuccessful || loginData == null || !loginData.success) {
                return Result.failure(Exception(loginData?.message ?: "Error en credenciales"))
            }

            val userId = loginData.userId
                ?: return Result.failure(Exception("Login correcto pero sin ID de usuario"))

            val sessionToken = loginData.sessionToken
            val tokens = loginData.tokens

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