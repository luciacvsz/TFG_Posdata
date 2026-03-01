package com.posdata.app.data.repository

import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.repository.contract.TokenConsumptionRepositoryContract

/**
 * Defines the token cost of each operation that requires cloud resources.
 *
 * Costs are designed around an initial balance of 100 tokens per user,
 * allowing approximately 10-15 SMS analyses before a top-up is required.
 *
 * Cost rationale:
 * - [POST_SMS] and [GET_SMS] invoke the AI model on AWS and represent the core
 *   functionality of the app.
 * - [PATCH_USER] covers profile, preferences and contact updates.
 * - [DELETE_USER] is a one-time destructive operation; cost is irrelevant
 *   since the account is removed immediately after.
 */
object CloudCosts {
    const val POST_SMS    = 3   // Submits an SMS for AI analysis
    const val GET_SMS     = 3   // Retrieves analysis result (no extra AI cost)
    const val PATCH_USER  = 8   // Updates profile, preferences or contacts
    const val DELETE_USER = 1   // Deletes the account from both services
}


/**
 * Repository responsible for verifying and consuming the user's token balance
 * before any cloud operation is performed.
 *
 * Acts as a gatekeeper: all repositories that interact with paid cloud resources
 * must call [haveEnoughTokens] before proceeding.
 *
 * @param userInfo Local data source used to read and update the token balance.
 */
class TokenConsumptionRepository(
    private val userInfo: UserDataStore,
): TokenConsumptionRepositoryContract {

    /**
     * Verifies that the user has sufficient tokens and deducts the given amount.
     *
     * The check and deduction are performed atomically by [UserDataStore.tryConsumeTokens],
     * preventing race conditions if multiple operations are triggered simultaneously.
     *
     * @param tokens Number of tokens required for the operation.
     * @return [Result.success] if the tokens were consumed successfully;
     *         [Result.failure] if the balance was insufficient.
     */
    override suspend fun haveEnoughTokens(tokens: Int): Result<String> {
        if (!userInfo.tryConsumeTokens(tokens)) {
            return Result.failure(
                Exception("Insufficient token balance. Please contact the service administrator.")
            )
        }
        return Result.success("Token balance updated successfully")
    }
}