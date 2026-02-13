package com.posdata.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.posdata.app.model.* import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "posdata_user_data")

class UserInfo(private val context: Context) {

    private val gson = Gson()

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val TOKENS = intPreferencesKey("tokens")

        val FULL_NAME = stringPreferencesKey("full_name")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val EMAIL = stringPreferencesKey("email")

        val PREF_COLOR_SCHEME = stringPreferencesKey("color_scheme")
        val PREF_FONT_SIZE = stringPreferencesKey("font_size")
        val PREF_NOTIFICATION_SOUND = stringPreferencesKey("notification_sound")
        val PREF_EXHAUSTIVITY = stringPreferencesKey("exhaustivity")
        val PREF_EXPLANATION_MODE = stringPreferencesKey("explanation")

        val TRUSTED_CONTACTS_JSON = stringPreferencesKey("trusted_contacts_json")
    }

    suspend fun saveUserSession(
        userId: String?,
        sessionToken: String,
        tokens: Int,
        fullName: String,
        contact: Contact,
        preferences: AppPreferences,
        trustedContacts: List<TrustedContact>
    ) {
        val trustedContactsJson = gson.toJson(trustedContacts)

        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userId as String
            prefs[SESSION_TOKEN] = sessionToken
            prefs[TOKENS] = tokens

            prefs[FULL_NAME] = fullName
            prefs[PHONE_NUMBER] = contact.phoneNumber
            prefs[EMAIL] = contact.email

            prefs[PREF_COLOR_SCHEME] = (preferences.colorScheme ?: AppColorScheme.LIGHT).name
            prefs[PREF_FONT_SIZE] = (preferences.fontSize ?: AppFontSize.REGULAR).name
            prefs[PREF_NOTIFICATION_SOUND] = (preferences.notificationSound ?: AppNotificationSound.ON).name
            prefs[PREF_EXHAUSTIVITY] = (preferences.exhaustivity ?: AppExhaustivity.REGULAR).name
            prefs[PREF_EXPLANATION_MODE] = (preferences.explanationMode ?: AppExplanationMode.ON).name

            prefs[TRUSTED_CONTACTS_JSON] = trustedContactsJson
        }
    }

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

    suspend fun updateTrustedContacts(
        trustedContacts: List<TrustedContact>
    ) {
        val json = gson.toJson(trustedContacts)
        context.dataStore.edit { prefs ->
            prefs[TRUSTED_CONTACTS_JSON] = json
        }
    }

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

    suspend fun tryConsumeToken(): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val current = prefs[TOKENS] ?: 0
            if (current > 0) {
                prefs[TOKENS] = current - 1
                success = true
            }
        }
        return success
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

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
        } catch (e: Exception) { emptyList() }

        UserData(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            userId = prefs[USER_ID] ?: "",
            sessionToken = prefs[SESSION_TOKEN] ?: "",
            tokens = prefs[TOKENS] ?: 0,
            fullName = prefs[FULL_NAME] ?: "",
            contact = contact,
            preferences = preferences,
            trustedContacts = trustedContacts
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
        if (name == null) return null
        return try {
            java.lang.Enum.valueOf(T::class.java, name)
        } catch (e: Exception) { null }
    }
}