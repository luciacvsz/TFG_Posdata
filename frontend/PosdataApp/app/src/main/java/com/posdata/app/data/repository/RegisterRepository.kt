package com.posdata.app.data.repository

import com.posdata.app.data.local.UserInfo
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.data.remote.ApiService
import com.posdata.app.data.remote.request.CheckEmailPOSTRequest
import com.posdata.app.data.remote.request.RegisterPOSTRequest
import com.posdata.app.data.remote.request.UserPOSTRequest

class RegisterRepository(
    private val localApi: ApiService,
    private val cloudApi: ApiService,
    private val userInfo: UserInfo
) {
    suspend fun performRegistration(fullName: String, phoneNumber: String, email: String, password: String ): Result<String> {
        return try {
            val localResp1 = localApi.postCheckEmail(CheckEmailPOSTRequest(email))
            val localData1 = localResp1.body()

            if (!localResp1.isSuccessful || localData1 == null) {
                return Result.failure(Exception(localData1?.message ?: "Error while trying to access local database"))
            } else if (localData1.success) {
                return Result.failure(Exception(localData1?.message ?: "The user already has an account"))
            }

            val cloudData = try {
                val cloudResp = cloudApi.postUser(UserPOSTRequest(fullName, phoneNumber, email))
                if (cloudResp.isSuccessful) cloudResp.body() else null
            } catch (e: Exception) {
                return Result.failure(Exception("Ha ocurrido un error tratando de dar de alta al usuario en el cloud"))
            }

            val userId = cloudData?.userId

            val contact = Contact(
                phoneNumber = phoneNumber,
                email = email
            )

            val localResp2 = localApi.postRegister(RegisterPOSTRequest(userId, email, password))
            val localData2 = localResp2.body()

            if (!localResp2.isSuccessful || localData2 == null || !localData2.success) {
                return Result.failure(Exception(localData2?.message ?: "Error while trying to insert user into local database"))
            }

            val sessionToken = localData2.sessionToken
            val tokens = localData2.tokens

            userInfo.saveUserSession (
                userId = userId,
                sessionToken = sessionToken,
                tokens = tokens,
                fullName = fullName,
                contact = contact,
                preferences = AppPreferences(),
                trustedContacts = emptyList()
            )

            Result.success("Registro completado")

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}