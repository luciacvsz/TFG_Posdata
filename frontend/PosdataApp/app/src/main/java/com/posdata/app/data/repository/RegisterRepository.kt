package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserInfo
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.request.LocalUserPOSTRequest
import com.posdata.app.data.remote.request.CloudUsersPOSTRequest
import com.posdata.app.data.remote.request.LocalLoginPOSTRequest
import com.posdata.app.sms.SMSReceiverManager

class RegisterRepository(
    private val context: Context,
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserInfo
) {
    suspend fun performRegistration(fullName: String, phoneNumber: String, email: String, password: String ): Result<String> {
        return try {
            val localResp1 = localApi.getUser(email)
            val localData1 = localResp1.body()

            if (!localResp1.isSuccessful || localData1 == null) {
                return Result.failure(Exception(localData1?.message ?: "Error while trying to access local database"))
            } else if (localData1.success) {
                return Result.failure(Exception(localData1?.message ?: "The user already has an account"))
            }

            val cloudData = try {
                val cloudResp = cloudApi.postUser(CloudUsersPOSTRequest(fullName, phoneNumber, email))
                val body = cloudResp.body()
                if (!cloudResp.isSuccessful || body == null) throw Exception()
                body
            } catch (e: Exception) {
                return Result.failure(Exception("Ha ocurrido un error tratando de dar de alta al usuario en el cloud"))
            }

            val userId = cloudData.userId

            val contact = Contact(
                phoneNumber = phoneNumber,
                email = email
            )

            val localResp2 = localApi.postUser(userId, LocalUserPOSTRequest(email, password))
            val localData2 = localResp2.body()

            if (!localResp2.isSuccessful || localData2 == null || !localData2.success) {
                return Result.failure(Exception(localData2?.message ?: "Error while trying to insert user into local database"))
            }

            val tokens = localData2.tokens

            userInfo.saveUserSession (
                userId = userId,
                tokens = tokens,
                fullName = fullName,
                contact = contact,
                preferences = AppPreferences(),
                trustedContacts = emptyList()
            )

            SMSReceiverManager.enableReceiver(context)

            Result.success("Registro completado")

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}