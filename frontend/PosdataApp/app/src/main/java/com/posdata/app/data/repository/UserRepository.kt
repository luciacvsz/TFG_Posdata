package com.posdata.app.data.repository

import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.ApiService
import com.posdata.app.data.remote.request.*
import com.posdata.app.model.*
import kotlinx.coroutines.flow.first

class UserRepository(
    private val localApi: ApiService,
    private val cloudApi: ApiService,
    private val userInfo: UserInfo
) {

    private suspend fun getCurrentUserId(): String? {
        val user = userInfo.userData.first()
        return if (user.isLoggedIn && user.userId.isNotEmpty()) user.userId else null
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phoneNumber: String? = null,
        email: String? = null
    ): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Usuario no identificado")

            val request = ProfilePATCHRequest(
                fullName = fullName,
                phoneNumber = phoneNumber,
                email = email
            )

            val response = cloudApi.patchProfile(userId, request)

            if (!response.isSuccessful) {
                throw Exception("Error al guardar perfil")
            }

            userInfo.updateProfile(
                fullName = fullName,
                phoneNumber = phoneNumber,
                email = email
            )

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updateSettings(
        fontSize: AppFontSize? = null,
        sound: AppNotificationSound? = null,
        color: AppColorScheme? = null,
        exhaustivity: AppExhaustivity? = null,
        explanation: AppExplanationMode? = null
    ): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Usuario no identificado")

            val preferencesDto = PreferencesDTO(
                fontSize = fontSize,
                notificationSound = sound,
                colorScheme = color,
                exhaustivity = exhaustivity,
                explanationMode = explanation
            )

            val request = PreferencesPATCHRequest(preferences = preferencesDto)

            val response = cloudApi.patchPreferences(userId, request)
            if (!response.isSuccessful){
                throw Exception("Error al guardar ajustes")
            }

            userInfo.updatePreferences(
                fontSize = fontSize,
                sound = sound,
                color = color,
                exhaustivity = exhaustivity,
                explanation = explanation
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncContacts(contacts: List<TrustedContact>): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Usuario no identificado")

            val dtos = contacts.map { domainContact ->
                TrustedContactDTO(
                    name = domainContact.name,
                    role = domainContact.role,
                    phoneNumber = domainContact.phoneNumber?.ifBlank { null },
                    email = domainContact.email?.ifBlank { null }
                )
            }

            val request = TrustedContactsPATCHRequest(trustedContacts = dtos)

            val response = cloudApi.patchTrustedContacts(userId, request)
            if (!response.isSuccessful) {
                throw Exception("Error al sincronizar contactos")
            }

            userInfo.updateTrustedContacts(contacts)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}