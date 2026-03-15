package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.repository.contract.DeleteAccountRepositoryContract
import com.posdata.app.data.repository.contract.TokenConsumptionRepositoryContract
import com.posdata.app.model.CloudOperation
import com.posdata.app.sms.SMSReceiverEnabler
import kotlinx.coroutines.flow.first

/**
 * Repository responsible for handling the full account deletion flow.
 *
 * Coordinates the deletion across both the local and cloud services,
 * verifies token availability before proceeding, clears the local
 * session and disables the SMS receiver upon success.
 *
 * @param context Application context required to disable the SMS receiver.
 * @param localApi Service interface for the local API.
 * @param cloudApi Service interface for the cloud API.
 * @param userInfo Local data source used to read session data and clear it on success.
 * @param tokenConsumptionRepository Repository used to verify and consume tokens.
 */
class DeleteAccountRepository(
    private val context: Context,
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserDataStore,
    private val tokenConsumptionRepository: TokenConsumptionRepositoryContract
): DeleteAccountRepositoryContract {

    /**
     * Retrieves the current user's ID from the local session.
     *
     * @return The user ID if the user is logged in and the ID is not empty, null otherwise.
     */
    private suspend fun getCurrentUserId(): String? {
        val user = userInfo.userData.first()
        return if (user.isLoggedIn && user.userId.isNotEmpty()) user.userId else null
    }

    /**
     * Executes the full account deletion flow.
     *
     * The operation follows this sequence:
     * 1. Verifies the user is authenticated.
     * 2. Checks and consumes the required token balance.
     * 3. Deletes the user record from the cloud service
     * 4. Deletes the user record from the local service.
     * 5. Clears the local session.
     *
     * @return [Result.success] with a confirmation message if the account was deleted;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun performDeleteAccount(): Result<String> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("No hay ninguna sesión activa"))

            val tokenResult = tokenConsumptionRepository.haveEnoughTokens(CloudOperation.DELETE_USER)
            if (tokenResult.isFailure) {
                return Result.failure(Exception("No se ha podido verificar el saldo de tokens")
                )
            }

            val cloudResponse = cloudApi.deleteUser(userId)
            if (!cloudResponse.isSuccessful) {
                return Result.failure(Exception("No se ha podido eliminar su cuenta. Inténtelo de nuevo más tarde"))
            }

            val localResponse = localApi.deleteUser(userId)
            if (!localResponse.isSuccessful) {
                return Result.failure(Exception("No se ha podido completar la eliminación de su cuenta. Inténtelo de nuevo más tarde"))
            }

            userInfo.clearSession()
            SMSReceiverEnabler.disableReceiver(context)

            Result.success("Cuenta eliminada con éxito")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Ha ocurrido un error inesperado. Inténtelo de nuevo más tarde"))
        }
    }
}