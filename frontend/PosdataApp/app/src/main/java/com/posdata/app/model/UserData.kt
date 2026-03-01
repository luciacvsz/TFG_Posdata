package com.posdata.app.model

/**
 * Represents the full local state of the authenticated user.
 *
 * This is the central model consumed by the UI layer via the reactive
 * [Flow][kotlinx.coroutines.flow.Flow] exposed by [UserLocalDataSource][com.posdata.app.data.local.UserDataStore].
 * Any change in the underlying DataStore automatically produces a new emission of this object.
 *
 * @param isLoggedIn Whether the user currently has an active session.
 * @param userId Unique identifier of the authenticated user.
 * @param tokens Current token balance available for cloud operations.
 * @param fullName Full name of the user.
 * @param contact Contact details of the user.
 * @param preferences Current application preferences of the user.
 * @param trustedContacts List of trusted contacts associated with the user.
 */
data class UserData(
    val isLoggedIn: Boolean,
    val userId: String,
    val tokens: Int,
    val fullName: String,
    val contact: Contact,
    val preferences: AppPreferences,
    val trustedContacts: List<TrustedContact>
)