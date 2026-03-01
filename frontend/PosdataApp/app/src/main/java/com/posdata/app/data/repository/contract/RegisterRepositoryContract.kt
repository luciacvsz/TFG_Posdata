package com.posdata.app.data.repository.contract

interface RegisterRepositoryContract {
    /**
     * Executes the full registration flow.
     *
     * @param fullName Full name of the user.
     * @param phoneNumber Phone number of the user.
     * @param email Email address of the user.
     * @param password Plain-text password chosen by the user.
     * @return [Result.success] with a confirmation message if the registration was successful;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    suspend fun performRegistration(
        fullName: String,
        phoneNumber: String,
        email: String,
        password: String
    ): Result<String>
}