package com.posdata.app.data.repository

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import com.posdata.app.data.local.UserInfo
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.request.LocalLoginPOSTRequest
import com.posdata.app.data.remote.request.LocalUserPATCHRequest
import com.posdata.app.sms.SMSReceiverManager

class LoginRepository(
    private val context: Context,
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserInfo,
    private val tokenConsumptionRepository: TokenConsumptionRepository
) {
    suspend fun performLoginAndSync(email: String, password: String): Result<String> {
        return try {
            val hashedPassword = HashUtils.sha512(password)
            val localResp = localApi.postLogin(LocalLoginPOSTRequest(email, hashedPassword))
            val localData = localResp.body()

            if (!localResp.isSuccessful || localData == null || !localData.success) {
                return Result.failure(Exception(localData?.message ?: "Error en credenciales"))
            }

            val userId = localData.userId
                ?: return Result.failure(Exception("Login correcto pero sin ID de usuario"))

            val tokens = localData.tokens

            val tokenResult = tokenConsumptionRepository.haveEnoughTokens(CloudCosts.GET_USER)
            if (tokenResult.isFailure) {
                return Result.failure(tokenResult.exceptionOrNull() ?: Exception("Error en tokens"))
            }

            val localResponse1 = localApi.patchUser(userId = userId,
                LocalUserPATCHRequest(null, null, CloudCosts.GET_USER)
            )
            if(!localResponse1.isSuccessful || localResponse1.body() == null) {
                return Result.failure(Exception(localResponse1.body()?.message ?: "Error inesperado actualizando tokens en la base de datos local."))
            }

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
                tokens = tokens,
                fullName = fullName,
                contact = contact,
                preferences = preferences,
                trustedContacts = trustedContacts
            )

            SMSReceiverManager.enableReceiver(context)

            Result.success("Login completado")

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}