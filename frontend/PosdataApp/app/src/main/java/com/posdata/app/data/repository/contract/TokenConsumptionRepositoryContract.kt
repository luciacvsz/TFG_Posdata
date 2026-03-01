package com.posdata.app.data.repository.contract

interface TokenConsumptionRepositoryContract {
    /**
     * Verifies that the user has sufficient tokens and deducts the given amount.
     *
     * @param tokens Number of tokens required for the operation.
     * @return [Result.success] if the tokens were consumed successfully;
     *         [Result.failure] if the balance was insufficient.
     */
    suspend fun haveEnoughTokens(tokens: Int): Result<String>
}