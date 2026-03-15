package com.posdata.app.data.repository

import android.content.Context
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.repository.contract.LogoutRepositoryContract
import com.posdata.app.sms.SMSReceiverEnabler

/**
 * Repository responsible for handling the logout flow.
 *
 * Clears the local session and disables the SMS receiver
 * to prevent background processing after the user signs out.
 *
 * @param context Application context required to disable the SMS receiver.
 * @param userInfo Local data source used to clear the session on logout.
 */
class LogoutRepository(
    private val context: Context,
    private val userInfo: UserDataStore
): LogoutRepositoryContract {

    /**
     * Executes the logout flow.
     *
     * Clears all locally persisted session data and disables the SMS receiver.
     * The SMS receiver is disabled after clearing the session to ensure no
     * incoming messages are processed once the user is no longer authenticated.
     *
     * @return [Result.success] with a confirmation message if the logout was successful;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun performLogout(): Result<String> {
        return try {
            userInfo.clearSession()
            SMSReceiverEnabler.disableReceiver(context)
            Result.success("Cierre de sesión completado con éxito")
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Ha ocurrido un error inesperado al cerrar sesión"))
        }
    }
}