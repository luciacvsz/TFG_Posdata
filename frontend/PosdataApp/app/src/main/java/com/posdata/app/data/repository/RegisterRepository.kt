package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.model.AppPreferences
import com.posdata.app.model.Contact
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.request.LocalUserPUTRequest
import com.posdata.app.data.remote.request.CloudUsersPOSTRequest
import com.posdata.app.data.repository.contract.RegisterRepositoryContract
import com.posdata.app.sms.SMSReceiverEnabler
import com.posdata.app.utils.HashUtils

/**
 * Repository responsible for handling the full user registration flow.
 *
 * Coordinates the creation of the user across both the local and cloud services,
 * persists the initial session, and enables the SMS receiver on success.
 *
 * Registration follows this order:
 * 1. Check the local database to ensure the email is not already registered.
 * 2. Create the user in the cloud service to obtain the assigned user ID.
 * 3. Create the user in the local database using the cloud-assigned ID.
 *
 * If the local creation fails after the cloud user has been created, the cloud record
 * will remain as an orphan.
 *
 * @param context Application context required to enable the SMS receiver on registration.
 * @param localApi Service interface for the local API.
 * @param cloudApi Service interface for the cloud API.
 * @param userInfo Local data source used to persist the initial user session.
 */
class RegisterRepository(
    private val context: Context,
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserDataStore
): RegisterRepositoryContract {

    /**
     * Executes the full registration flow.
     *
     * The operation follows this sequence:
     * 1. Checks that no local account exists for the given email.
     * 2. Creates the user profile in the cloud service.
     * 3. Hashes the password and creates the user credentials in the local database,
     *    using the ID assigned by the cloud.
     * 4. Persists the initial session in the local DataStore.
     * 5. Enables the SMS receiver.
     *
     * @param fullName Full name of the user.
     * @param phoneNumber Phone number of the user.
     * @param email Email address of the user.
     * @param password Plain-text password chosen by the user.
     * @return [Result.success] with a confirmation message if the registration was successful;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun performRegistration(fullName: String, phoneNumber: String, email: String, password: String ): Result<String> {
        return try {
            val localCheckResponse = localApi.getUser(email)
            val localCheckData = localCheckResponse.body()

            if (!localCheckResponse.isSuccessful || localCheckData == null) {
                return Result.failure(
                    Exception("No se ha podido verificar el correo. Inténtelo de nuevo más tarde")
                )
            } else if (localCheckData.success) {
                return Result.failure(
                    Exception("Ya existe una cuenta con este correo electrónico")
                )
            }

            val cloudResponse = cloudApi.postUser(CloudUsersPOSTRequest(fullName, phoneNumber, email))
            val cloudData = cloudResponse.body()

            if (!cloudResponse.isSuccessful || cloudData == null) {
                return Result.failure(Exception("No se ha podido completar el registro. Inténtelo de nuevo más tarde"))
            }

            val userId = cloudData.userId
            val hashedPassword = HashUtils.sha512(password)
            val contact = Contact(
                phoneNumber = phoneNumber,
                email = email
            )

            val localCreateResponse = localApi.putUser(userId, LocalUserPUTRequest(email, hashedPassword))
            val localCreateData = localCreateResponse.body()

            if (!localCreateResponse.isSuccessful || localCreateData == null || !localCreateData.success) {
                return Result.failure(
                    Exception("No se ha podido completar el registro. Inténtelo de nuevo más tarde")
                )
            }

            userInfo.saveUserSession (
                userId = userId,
                fullName = fullName,
                contact = contact,
                preferences = AppPreferences(),
                trustedContacts = emptyList()
            )

            SMSReceiverEnabler.enableReceiver(context)

            Result.success("Registro completado con éxito")

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Ha ocurrido un error inesperado durante el registro. Inténtelo de nuevo más tarde"))
        }
    }
}