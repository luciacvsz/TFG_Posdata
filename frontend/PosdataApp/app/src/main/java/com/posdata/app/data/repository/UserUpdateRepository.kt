package com.posdata.app.data.repository

import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.request.*
import com.posdata.app.model.*
import kotlinx.coroutines.flow.first

class UserUpdateRepository(
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserInfo
) {

    private suspend fun getCurrentUserId(): String? {
        val user = userInfo.userData.first()
        return if (user.isLoggedIn && user.userId.isNotEmpty()) user.userId else null
    }

    suspend fun updateProfile(
        fullName: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        password: String? = null
    ): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Usuario no identificado")

            if (password == null) {
                val cloudRequest = CloudProfilePATCHRequest(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    email = email
                )

                val cloudResponse = cloudApi.patchProfile(userId, cloudRequest)
                if (!cloudResponse.isSuccessful) {
                    throw Exception("Error al guardar perfil")
                }
            }

            if (fullName == null && phoneNumber == null) {
                val passwordToSave = password?.let { HashUtils.sha512(it)}
                val localRequest = LocalProfilePATCHRequest(
                    email = email,
                    password = passwordToSave
                )

                val localResponse = localApi.patchUser(userId, localRequest)
                if(!localResponse.isSuccessful) {
                    throw Exception("Error al guardar perfil")
                }
            }

            if( password == null) {
                userInfo.updateProfile(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    email = email
                )
            }

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun updatePreferences(
        colorScheme: AppColorScheme? = null,
        fontSize: AppFontSize? = null,
        notificationSound: AppNotificationSound? = null,
        exhaustivity: AppExhaustivity? = null,
        explanationMode: AppExplanationMode? = null
    ): Result<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: throw Exception("Usuario no identificado")

            val preferencesDto = PreferencesDTO(
                colorScheme = colorScheme,
                fontSize = fontSize,
                notificationSound = notificationSound,
                exhaustivity = exhaustivity,
                explanationMode = explanationMode
            )

            val request = CloudPreferencesPATCHRequest(preferences = preferencesDto)

            val response = cloudApi.patchPreferences(userId, request)
            if (!response.isSuccessful){
                throw Exception("Error al guardar ajustes")
            }

            userInfo.updatePreferences(
                colorScheme = colorScheme,
                fontSize = fontSize,
                notificationSound = notificationSound,
                exhaustivity = exhaustivity,
                explanationMode = explanationMode
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

            val request = CloudTrustedContactsPATCHRequest(trustedContacts = dtos)

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