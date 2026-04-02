package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.repository.contract.LogoutRepositoryContract
import com.posdata.app.sms.SMSReceiverEnabler

/**
 * Repository responsible for handling the logout flow.
 *
 * Clears the local session.
 *
 * @param userInfo Local data source used to clear the session on logout.
 */
class LogoutRepository(
    private val userInfo: UserDataStore
): LogoutRepositoryContract {

    /**
     * Executes the logout flow.
     *
     * Clears all locally persisted session data.
     *
     * @return [Result.success] with a confirmation message if the logout was successful;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun performLogout(): Result<String> {
        return try {
            userInfo.clearSession()
            Result.success("Cierre de sesión completado con éxito")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Ha ocurrido un error inesperado al cerrar sesión"))
        }
    }
}