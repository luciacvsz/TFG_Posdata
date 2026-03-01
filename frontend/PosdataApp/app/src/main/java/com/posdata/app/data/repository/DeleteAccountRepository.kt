package com.posdata.app.data.repository

import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.repository.contract.DeleteAccountRepositoryContract
import kotlinx.coroutines.flow.first

/**
 * Repository responsible for handling the full account deletion flow.
 *
 * Coordinates the deletion across both the local and cloud services,
 * verifies token availability before proceeding, and clears the local
 * session upon success.
 *
 * @param localApi Service interface for the local API.
 * @param cloudApi Service interface for the cloud API.
 * @param userInfo Local data source used to read session data and clear it on success.
 * @param tokenConsumptionRepository Repository used to verify and consume tokens.
 */
class DeleteAccountRepository(
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserDataStore,
    private val tokenConsumptionRepository: TokenConsumptionRepository
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
                ?: return Result.failure(Exception("User not authenticated"))

            val tokenResult = tokenConsumptionRepository.haveEnoughTokens(CloudCosts.DELETE_USER)
            if (tokenResult.isFailure) {
                return Result.failure(
                    tokenResult.exceptionOrNull() ?: Exception("Failed to process token balance")
                )
            }

            val cloudResponse = cloudApi.deleteUser(userId)
            if (!cloudResponse.isSuccessful) {
                return Result.failure(Exception("Failed to delete user from cloud service"))
            }

            val localResponse = localApi.deleteUser(userId)
            if (!localResponse.isSuccessful) {
                return Result.failure(Exception("Failed to delete user from local database"))
            }

            userInfo.clearSession()

            Result.success("Account deleted successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}