package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.request.LocalLoginPOSTRequest
import com.posdata.app.data.repository.contract.LoginRepositoryContract
import com.posdata.app.sms.SMSReceiverEnabler
import com.posdata.app.utils.HashUtils

/**
 * Repository responsible for handling the login and post-login sync flow.
 *
 * Authenticates the user against the local server, fetches the full user profile
 * from the cloud, and persists the session locally.
 *
 * @param context Application context required to enable the SMS receiver on login.
 * @param localApi Service interface for the local API.
 * @param cloudApi Service interface for the cloud API.
 * @param userInfo Local data source used to persist the user session.
 */
class LoginRepository(
    private val context: Context,
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserDataStore,
): LoginRepositoryContract {

    /**
     * Executes the full login and synchronization flow.
     *
     * The operation follows this sequence:
     * 1. Hashes the password and authenticates against the local server.
     * 2. Fetches the full user profile from the cloud service.
     * 3. Persists the session in the local DataStore.
     * 4. Enables the SMS receiver.
     *
     * @param email Email address of the user.
     * @param password Plain-text password of the user.
     * @return [Result.success] with a confirmation message if the login was successful;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun performLoginAndSync(email: String, password: String): Result<String> {
        return try {
            val hashedPassword = HashUtils.sha512(password)
            val localResp = localApi.postLogin(LocalLoginPOSTRequest(email, hashedPassword))
            val localData = localResp.body()

            if (!localResp.isSuccessful || localData == null || !localData.success) {
                return Result.failure(Exception(localData?.message ?: "Invalid credentials"))
            }

            val userId = localData.userId
            val tokens = localData.tokens

            val cloudData = try {
                val cloudResp = cloudApi.getUser(userId)
                if (cloudResp.isSuccessful) cloudResp.body() else null
            } catch (_: Exception) {
                return Result.failure(
                    Exception(
                        "Unexpected error trying to retreive user details from the cloud service.")
                )
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

            SMSReceiverEnabler.enableReceiver(context)

            Result.success("Login successful")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}