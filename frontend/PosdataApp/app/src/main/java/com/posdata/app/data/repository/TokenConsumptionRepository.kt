package com.posdata.app.data.repository

import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.request.LocalUserTokensPATCHRequest
import com.posdata.app.data.repository.contract.TokenConsumptionRepositoryContract
import com.posdata.app.model.CloudOperation
import kotlinx.coroutines.flow.first

/**
 * Repository responsible for verifying and consuming the user's token balance
 * before any cloud operation is performed.
 *
 * Acts as a gatekeeper: all repositories that interact with paid cloud resources
 * must call [haveEnoughTokens] before proceeding.
 *
 * @param userInfo Local data source used to read the current user session.
 * @param localApi API client used to communicate with the local authentication server.
 */
class TokenConsumptionRepository(
    private val userInfo: UserDataStore,
    private val localApi: LocalApiService
): TokenConsumptionRepositoryContract {

    /**
     * Verifies that the user has sufficient tokens and deducts the corresponding amount
     * for the given operation on the local authentication server.
     *
     * The check and deduction are performed atomically on the server side,
     * preventing race conditions if multiple operations are triggered simultaneously.
     *
     * @param operation The billable cloud operation to perform.
     * @param userId Optional user ID to use when the session is not yet available,
     * such as during the login sync flow. If null, the ID is read from the local session.
     * @return [Result.success] if the tokens were consumed successfully;
     * [Result.failure] if the balance was insufficient or an error occurred.
     */
    override suspend fun haveEnoughTokens(operation: CloudOperation, userId: String?): Result<String> {
        val resolvedUserId = userId
            ?: userInfo.userData.first().userId
            ?: return Result.failure(Exception("No hay ninguna sesión activa"))

        val response = try {
            localApi.patchUserTokens(resolvedUserId, LocalUserTokensPATCHRequest(operation.name))
        } catch (e: Exception) {
            return Result.failure(Exception("Ha ocurrido un error inesperado al verificar el saldo de tokens"))
        }

        val body = response.body()

        if (!response.isSuccessful || body == null || !body.success) {
            return Result.failure(
                Exception(body?.message ?: "No se ha podido verificar el saldo de tokens")
            )
        }

        return Result.success("Tokens descontados correctamente")
    }
}