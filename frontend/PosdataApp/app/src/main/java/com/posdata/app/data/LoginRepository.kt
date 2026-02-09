package com.posdata.app.data

import com.posdata.app.model.AppPreferences
import com.posdata.app.network.ApiService
import com.posdata.app.network.LoginRequest

class LoginRepository(
    private val localApi: ApiService,
    private val cloudApi: ApiService,
    private val userPrefs: UserPreferences
) {

    suspend fun performLoginAndSync(email: String, pass: String): Result<String> {
        return try {
            val loginResp = localApi.login(LoginRequest(email, pass))
            val loginData = loginResp.body()

            if (!loginResp.isSuccessful || loginData == null || !loginData.success) {
                return Result.failure(Exception(loginData?.message ?: "Error en credenciales"))
            }

            val userId = loginData.user_id
                ?: return Result.failure(Exception("Login correcto pero sin ID de usuario"))

            var cloudData = try {
                val cloudResp = cloudApi.user(userId)
                if (cloudResp.isSuccessful) cloudResp.body() else null
            } catch (e: Exception) {
                return Result.failure(Exception("Ha ocurrido un error accediendo a los datos del usuario en el cloud"))
            }

            val fullName = cloudData?.fullName ?: "Usuario"
            val phoneNumber = cloudData?.contact ?: "+00 000 000 000"
            val trustedContacts = cloudData?.trustedContacts ?: emptyList()
            val preferences = cloudData?.preferences  ?: AppPreferences()

            userPrefs.saveUserSession(
                id = userId,
                name = fullName,
                email = emailInput,
                phone = phoneNumber,
                apiToken = loginData.token,
                tokens = loginData.tokens ?: 0,
                contacts = trustedContacts,
                preferences = preferences
            )

            Result.success("Login completado")

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}