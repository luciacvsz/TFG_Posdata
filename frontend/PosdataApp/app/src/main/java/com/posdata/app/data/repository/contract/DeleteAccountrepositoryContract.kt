package com.posdata.app.data.repository.contract

interface DeleteAccountRepositoryContract {
    /**
     * Executes the full account deletion flow.
     *
     * @return [Result.success] with a confirmation message if the account was deleted;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    suspend fun performDeleteAccount(): Result<String>
}