package com.posdata.app.data.repository

import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.CloudApiService
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.request.*
import com.posdata.app.data.repository.contract.TokenConsumptionRepositoryContract
import com.posdata.app.data.repository.contract.UserUpdateRepositoryContract
import com.posdata.app.model.*
import com.posdata.app.utils.HashUtils
import kotlinx.coroutines.flow.first

/**
 * Repository responsible for handling all user data update operations.
 *
 * Coordinates partial updates across the local database, the cloud service,
 * and the local DataStore, verifying and consuming the required token balance
 * before any cloud operation is performed.
 *
 * @param localApi Service interface for the local API.
 * @param cloudApi Service interface for the cloud API.
 * @param userInfo Local data source used to read session data and persist updates.
 * @param tokenConsumptionRepository Repository used to verify and consume tokens.
 */
class UserUpdateRepository(
    private val localApi: LocalApiService,
    private val cloudApi: CloudApiService,
    private val userInfo: UserDataStore,
    private val tokenConsumptionRepository: TokenConsumptionRepositoryContract
): UserUpdateRepositoryContract {

    /**
     * Retrieves the current user's ID from the local session.
     *
     * @return The user ID if the user is logged in and the ID is not empty, null otherwise.
     */
    private suspend fun getCurrentUserId(): String? {
        val user = userInfo.userData.first()
        return if (user.isLoggedIn && user.userId.isNotEmpty()) user.userId else null
    }

    /**
     * Updates the user's profile data and/or credentials.
     *
     * Handles two mutually exclusive flows depending on the fields provided.
     * These flows are always triggered as separate operations from the UI:
     *
     * - **Credential update** (email and/or password):
     *   - Password is always updated in the local database only.
     *   - Email is updated in the local database, the cloud profile, and the DataStore.
     *     Requires token consumption only if email is provided.
     *
     * - **Profile update** (fullName and/or phoneNumber):
     *   - Updates the cloud profile and the DataStore.
     *   - Always requires token consumption.
     *
     * @param fullName New full name, or null to leave it unchanged.
     * @param phoneNumber New phone number, or null to leave it unchanged.
     * @param email New email address, or null to leave it unchanged.
     * @param password New plain-text password, or null to leave it unchanged.
     * @return [Result.success] if the update was applied successfully;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun updateProfile(
        fullName: String?,
        phoneNumber: String?,
        email: String?,
        password: String?
    ): Result<Boolean> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("No hay ninguna sesión activa"))

            val hashedPassword = password?.let { HashUtils.sha512(it) }
            val isCredentialUpdate = email != null || hashedPassword != null
            val isProfileUpdate = fullName != null || phoneNumber != null

            if (isCredentialUpdate) {

                if (email != null) {

                    val tokenResult = tokenConsumptionRepository.haveEnoughTokens(CloudOperation.PATCH_USER)
                    if (tokenResult.isFailure) {
                        return Result.failure(
                            tokenResult.exceptionOrNull() ?: Exception("No se ha podido verificar el saldo de tokens")
                        )
                    }

                    val cloudResponse = cloudApi.patchProfile(
                        userId  = userId,
                        request = CloudProfilePATCHRequest(email = email)
                    )
                    if (!cloudResponse.isSuccessful) {
                        return Result.failure(Exception("No se ha podido actualizar el correo electrónico. Inténtelo de nuevo más tarde"))
                    }

                    userInfo.updateProfile(email = email)
                }

                val localResponse = localApi.patchUser(
                    userId  = userId,
                    request = LocalUserPATCHRequest(
                        email    = email,
                        password = hashedPassword,
                    )
                )
                if (!localResponse.isSuccessful) {
                    return Result.failure(Exception("No se han podido actualizar las credenciales. Inténtelo de nuevo más tarde"))
                }
            }

            if (isProfileUpdate) {

                val tokenResult = tokenConsumptionRepository.haveEnoughTokens(CloudOperation.PATCH_USER)
                if (tokenResult.isFailure) {
                    return Result.failure(
                        tokenResult.exceptionOrNull()
                            ?: Exception("No se ha podido verificar el saldo de tokens")
                    )
                }

                val cloudResponse = cloudApi.patchProfile(
                    userId  = userId,
                    request = CloudProfilePATCHRequest(
                        fullName    = fullName,
                        phoneNumber = phoneNumber,
                        email       = null
                    )
                )
                if (!cloudResponse.isSuccessful) {
                    return Result.failure(Exception("No se ha podido actualizar el perfil. Inténtelo de nuevo más tarde"))
                }

                userInfo.updateProfile(
                    fullName    = fullName,
                    phoneNumber = phoneNumber
                )
            }

            Result.success(true)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Ha ocurrido un error inesperado al actualizar el perfil. Inténtelo de nuevo más tarde"))
        }
    }

    /**
     * Updates the user's application preferences in the cloud and syncs
     * the result to the local DataStore.
     *
     * Requires token consumption. Only non-null fields are applied.
     *
     * @param colorScheme New color scheme, or null to leave it unchanged.
     * @param fontSize New font size, or null to leave it unchanged.
     * @param notificationSound New notification sound setting, or null to leave it unchanged.
     * @param exhaustivity New exhaustivity level, or null to leave it unchanged.
     * @param explanationMode New explanation mode, or null to leave it unchanged.
     * @return [Result.success] if the preferences were updated successfully;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun updatePreferences(
        colorScheme: AppColorScheme?,
        fontSize: AppFontSize?,
        notificationSound: AppNotificationSound?,
        exhaustivity: AppExhaustivity?,
        explanationMode: AppExplanationMode?
    ): Result<Boolean> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("No hay ninguna sesión activa"))

            val tokenResult = tokenConsumptionRepository.haveEnoughTokens(CloudOperation.PATCH_USER)
            if (tokenResult.isFailure) {
                return Result.failure(Exception("No se ha podido verificar el saldo de tokens")
                )
            }

            val cloudResponse = cloudApi.patchPreferences(
                userId  = userId,
                request = CloudPreferencesPATCHRequest(
                    preferences = PreferencesDTO(
                        colorScheme       = colorScheme,
                        fontSize          = fontSize,
                        notificationSound = notificationSound,
                        exhaustivity      = exhaustivity,
                        explanationMode   = explanationMode
                    )
                )
            )
            if (!cloudResponse.isSuccessful) {
                return Result.failure(Exception("No se han podido guardar sus preferencias. Inténtelo de nuevo más tarde"))
            }

            userInfo.updatePreferences(
                colorScheme = colorScheme,
                fontSize = fontSize,
                notificationSound = notificationSound,
                exhaustivity = exhaustivity,
                explanationMode = explanationMode
            )

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ha ocurrido un error inesperado al guardar las preferencias. Inténtelo de nuevo más tarde"))
        }
    }

    /**
     * Replaces the user's full list of trusted contacts in the cloud and syncs
     * the result to the local DataStore.
     *
     * Maps domain model objects to their DTO equivalents before sending to the API.
     * Blank phone numbers and emails are normalized to null, as the server does not
     * accept empty strings for optional fields.
     *
     * Requires token consumption.
     *
     * @param contacts New list of trusted contacts to persist.
     * @return [Result.success] if the contacts were synced successfully;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    override suspend fun syncContacts(contacts: List<TrustedContact>): Result<Boolean> {
        return try {
            val userId = getCurrentUserId()
                ?: return Result.failure(Exception("No hay ninguna sesión activa"))

            val tokenResult = tokenConsumptionRepository.haveEnoughTokens(CloudOperation.PATCH_USER)
            if (tokenResult.isFailure) {
                return Result.failure(Exception("No se ha podido verificar el saldo de tokens")
                )
            }

            val dtos = contacts.map { contact ->
                TrustedContactDTO(
                    name = contact.name,
                    role = contact.role,
                    phoneNumber = contact.phoneNumber?.ifBlank { null },
                    email = contact.email?.ifBlank { null }
                )
            }

            val cloudResponse = cloudApi.patchTrustedContacts(
                userId  = userId,
                request = CloudTrustedContactsPATCHRequest(trustedContacts = dtos)
            )
            if (!cloudResponse.isSuccessful) {
                return Result.failure(Exception("No se han podido guardar los contactos de seguridad. Inténtelo de nuevo más tarde"))
            }

            userInfo.updateTrustedContacts(contacts)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(Exception("Ha ocurrido un error inesperado al guardar los contactos de seguridad. Inténtelo de nuevo más tarde"))
        }
    }
}