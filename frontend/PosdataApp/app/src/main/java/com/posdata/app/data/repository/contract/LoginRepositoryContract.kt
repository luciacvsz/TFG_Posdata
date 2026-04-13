package com.posdata.app.data.repository.contract

interface LoginRepositoryContract {
    /**
     * Executes the full login and synchronization flow.
     *
     * @param email Email address of the user.
     * @param password Plain-text password of the user.
     * @return [Result.success] with a confirmation message if the login was successful;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    suspend fun performLoginAndSync(email: String, password: String): Result<String>
}