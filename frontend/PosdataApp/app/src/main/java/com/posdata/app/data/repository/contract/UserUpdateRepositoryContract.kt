package com.posdata.app.data.repository.contract

import com.posdata.app.model.AppColorScheme
import com.posdata.app.model.AppExhaustivity
import com.posdata.app.model.AppExplanationMode
import com.posdata.app.model.AppFontSize
import com.posdata.app.model.AppNotificationSound
import com.posdata.app.model.TrustedContact

interface UserUpdateRepositoryContract {
    /**
     * Updates the user's profile data and/or credentials.
     *
     * @param fullName New full name, or null to leave it unchanged.
     * @param phoneNumber New phone number, or null to leave it unchanged.
     * @param email New email address, or null to leave it unchanged.
     * @param password New plain-text password, or null to leave it unchanged.
     * @return [Result.success] if the update was applied successfully;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    suspend fun updateProfile(
        fullName: String?    = null,
        phoneNumber: String? = null,
        email: String?       = null,
        password: String?    = null
    ): Result<Boolean>

    /**
     * Updates the user's application preferences.
     *
     * @param colorScheme New color scheme, or null to leave it unchanged.
     * @param fontSize New font size, or null to leave it unchanged.
     * @param notificationSound New notification sound setting, or null to leave it unchanged.
     * @param exhaustivity New exhaustivity level, or null to leave it unchanged.
     * @param explanationMode New explanation mode, or null to leave it unchanged.
     * @return [Result.success] if the preferences were updated successfully;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    suspend fun updatePreferences(
        colorScheme: AppColorScheme?             = null,
        fontSize: AppFontSize?                   = null,
        notificationSound: AppNotificationSound? = null,
        exhaustivity: AppExhaustivity?           = null,
        explanationMode: AppExplanationMode?     = null
    ): Result<Boolean>

    /**
     * Replaces the user's full list of trusted contacts.
     *
     * @param contacts New list of trusted contacts to persist.
     * @return [Result.success] if the contacts were synced successfully;
     *         [Result.failure] with a descriptive exception if any step fails.
     */
    suspend fun syncContacts(contacts: List<TrustedContact>): Result<Boolean>
}