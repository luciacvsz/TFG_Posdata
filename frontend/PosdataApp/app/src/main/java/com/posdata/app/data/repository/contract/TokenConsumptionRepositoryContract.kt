package com.posdata.app.data.repository.contract

import com.posdata.app.model.CloudOperation

interface TokenConsumptionRepositoryContract {
    /**
     * Verifies that the user has sufficient tokens and deducts the corresponding amount
     * for the given operation on the local authentication server.
     *
     * @param operation The billable cloud operation to perform.
     * @param userId Optional user ID to use when the session is not yet available,
     * such as during the login sync flow. If null, the ID is read from the local session.
     * @return [Result.success] if the tokens were consumed successfully;
     * [Result.failure] if the balance was insufficient or an error occurred.
     */
    suspend fun haveEnoughTokens(operation: CloudOperation, userId: String? = null): Result<String>
}