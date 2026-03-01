package com.posdata.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.posdata.app.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Extension property that exposes the DataStore instance associated with the [Context]. */
val Context.dataStore by preferencesDataStore(name = "posdata_user_data")

/**
 * Local data source responsible for persisting the user session
 * using Jetpack DataStore (Preferences).
 *
 * Exposes a reactive [Flow] with the full user state and provides
 * atomic write operations to update each data subset independently.
 *
 * @param context Application context required to access the DataStore.
 */
class UserDataStore(private val context: Context) {

    companion object {
        private val gson = Gson()

        // --- Session keys ---
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val TOKENS = intPreferencesKey("tokens")

        // --- Profile keys ---
        val FULL_NAME = stringPreferencesKey("full_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val EMAIL = stringPreferencesKey("email")

        // --- Preference keys ---
        val PREF_COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val PREF_FONT_SIZE = stringPreferencesKey("font_size")
        val PREF_NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
        val PREF_EXHAUSTIVITY = stringPreferencesKey("exhaustivity")
        val PREF_EXPLANATION_MODE = stringPreferencesKey("explanation")

        // --- Contacts keys ---
        val TRUSTED_CONTACTS_JSON = stringPreferencesKey("trusted_contacts_json")
    }

    /**
     * Persists the complete set of session data after a successful login.
     *
     * Serializes [trustedContacts] to JSON since DataStore does not support composite types.
     * All writes are executed in a single atomic transaction via [edit].
     *
     * @param userId Unique identifier of the authenticated user.
     * @param tokens Initial token balance associated with the account.
     * @param fullName Full name of the user.
     * @param contact Contact details: phone number and email address.
     * @param preferences Application preferences. Null fields are replaced by default values.
     * @param trustedContacts List of trusted contacts for the user.
     */
    suspend fun saveUserSession(
        userId: String?,
        tokens: Int,
        fullName: String,
        contact: Contact,
        preferences: AppPreferences,
        trustedContacts: List<TrustedContact>
    ) {
        val trustedContactsJson = gson.toJson(trustedContacts)

        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userId ?: ""
            prefs[TOKENS] = tokens

            prefs[FULL_NAME] = fullName
            prefs[PHONE_NUMBER] = contact.phoneNumber
            prefs[EMAIL] = contact.email

            prefs[PREF_COLOR_SCHEME] = preferences.colorScheme.name
            prefs[PREF_FONT_SIZE] = preferences.fontSize.name
            prefs[PREF_NOTIFICATION_SOUND] = preferences.notificationSound.name
            prefs[PREF_EXHAUSTIVITY] = preferences.exhaustivity.name
            prefs[PREF_EXPLANATION_MODE] = preferences.explanationMode.name

            prefs[TRUSTED_CONTACTS_JSON] = trustedContactsJson
        }
    }

    /**
     * Partially updates the user's profile data.
     *
     * Only fields with a non-null value are persisted, allowing selective
     * updates without overwriting unmodified data.
     *
     * @param fullName New full name, or null to leave it unchanged.
     * @param phoneNumber New phone number, or null to leave it unchanged.
     * @param email New email address, or null to leave it unchanged.
     */
    suspend fun updateProfile(
        fullName: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
    ) {
        context.dataStore.edit { prefs ->
            if (fullName != null) prefs[FULL_NAME] = fullName
            if (phoneNumber != null) prefs[PHONE_NUMBER] = phoneNumber
            if (email != null) prefs[EMAIL] = email
        }
    }

    /**
     * Replaces the locally stored list of trusted contacts.
     *
     * The list is serialized to JSON since DataStore only supports primitive types.
     *
     * @param trustedContacts New list of trusted contacts.
     */
    suspend fun updateTrustedContacts(trustedContacts: List<TrustedContact>) {
        val json = gson.toJson(trustedContacts)
        context.dataStore.edit { prefs ->
            prefs[TRUSTED_CONTACTS_JSON] = json
        }
    }

    /**
     * Partially updates the application preferences.
     *
     * Only fields with a non-null value are persisted.
     *
     * @param colorScheme New color scheme, or null to leave it unchanged.
     * @param fontSize New font size, or null to leave it unchanged.
     * @param notificationSound New notification sound setting, or null to leave it unchanged.
     * @param exhaustivity New exhaustivity level, or null to leave it unchanged.
     * @param explanationMode New explanation mode, or null to leave it unchanged.
     */
    suspend fun updatePreferences(
        colorScheme: AppColorScheme? = null,
        fontSize: AppFontSize? = null,
        notificationSound: AppNotificationSound? = null,
        exhaustivity: AppExhaustivity? = null,
        explanationMode: AppExplanationMode? = null
    ) {
        context.dataStore.edit { prefs ->
            if (colorScheme != null) prefs[PREF_COLOR_SCHEME] = colorScheme.name
            if (fontSize != null) prefs[PREF_FONT_SIZE] = fontSize.name
            if (notificationSound != null) prefs[PREF_NOTIFICATION_SOUND] = notificationSound.name
            if (exhaustivity != null) prefs[PREF_EXHAUSTIVITY] = exhaustivity.name
            if (explanationMode != null) prefs[PREF_EXPLANATION_MODE] = explanationMode.name
        }
    }

    /**
     * Attempts to deduct the given amount from the user's token balance.
     *
     * The read and write are performed in a single atomic operation via [updateData],
     * ensuring no race conditions occur.
     *
     * @param amount Number of tokens to consume.
     * @return `true` if the balance was sufficient and has been deducted;
     *         `false` if the balance was insufficient and remains unchanged.
     */
    suspend fun tryConsumeTokens(amount: Int): Boolean {
        var success = false
        context.dataStore.updateData { prefs ->
            val current = prefs[TOKENS] ?: 0
            if (current >= amount) {
                success = true
                prefs.toMutablePreferences().apply {
                    this[TOKENS] = current - amount
                }
            } else {
                prefs
            }
        }
        return success
    }

    /**
     * Clears all persisted data, invalidating the current session.
     */
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    /**
     * Reactive flow that emits the full user state whenever the DataStore changes.
     *
     * Transforms raw preferences into a structured [UserData] object.
     * Trusted contacts are deserialized from their JSON representation,
     * returning an empty list on any parsing error.
     * Missing or invalid enum fields fall back to their default values.
     */
    val userData: Flow<UserData> = context.dataStore.data.map { prefs ->

        val contact = Contact(
            phoneNumber = prefs[PHONE_NUMBER] ?: "",
            email = prefs[EMAIL] ?: ""
        )

        val preferences = AppPreferences(
            colorScheme = enumValueOfOrNull<AppColorScheme>(prefs[PREF_COLOR_SCHEME]) ?: AppColorScheme.LIGHT,
            fontSize = enumValueOfOrNull<AppFontSize>(prefs[PREF_FONT_SIZE]) ?: AppFontSize.REGULAR,
            notificationSound = enumValueOfOrNull<AppNotificationSound>(prefs[PREF_NOTIFICATION_SOUND]) ?: AppNotificationSound.ON,
            exhaustivity = enumValueOfOrNull<AppExhaustivity>(prefs[PREF_EXHAUSTIVITY]) ?: AppExhaustivity.REGULAR,
            explanationMode = enumValueOfOrNull<AppExplanationMode>(prefs[PREF_EXPLANATION_MODE]) ?: AppExplanationMode.ON
        )

        val jsonTrustedContacts = prefs[TRUSTED_CONTACTS_JSON] ?: "[]"
        val typeToken = object : TypeToken<List<TrustedContact>>() {}.type
        val trustedContacts: List<TrustedContact> = try {
            gson.fromJson(jsonTrustedContacts, typeToken) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        UserData(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            userId = prefs[USER_ID] ?: "",
            tokens = prefs[TOKENS] ?: 0,
            fullName = prefs[FULL_NAME] ?: "",
            contact = contact,
            preferences = preferences,
            trustedContacts = trustedContacts
        )
    }

    /**
     * Converts a [String] into the corresponding value of enum [T], or `null` if not found.
     *
     * Used internally to deserialize enum values stored as plain text.
     *
     * @param name Name of the enum value, or `null`.
     * @return The matching enum value, or `null` if the name is invalid or null.
     */
    private inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
        if (name == null) return null
        return try {
            java.lang.Enum.valueOf(T::class.java, name)
        } catch (_: Exception) {
            null
        }
    }
}