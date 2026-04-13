package com.posdata.app.data.repository.contract

interface LogoutRepositoryContract {
    /**
     * Executes the logout flow.
     *
     * @return [Result.success] with a confirmation message if the logout was successful;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    suspend fun performLogout(): Result<String>
}