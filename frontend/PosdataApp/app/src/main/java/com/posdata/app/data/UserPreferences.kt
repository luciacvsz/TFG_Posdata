package com.posdata.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.posdata.app.model.* import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "posdata_settings")

class UserInfo(private val context: Context) {

    private val gson = Gson()

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val SESSION_TOKEN = stringPreferencesKey("session_token")
        val TOKENS = intPreferencesKey("user_tokens")

        val FULL_NAME = stringPreferencesKey("user_full_name")
        val PHONE_NUMBER = stringPreferencesKey("user_phone_number")
        val EMAIL = stringPreferencesKey("user_email")

        val PREF_FONT_SIZE = stringPreferencesKey("font_size")
        val PREF_SOUND = stringPreferencesKey("notification_sound")
        val PREF_COLOR = stringPreferencesKey("color_scheme")
        val PREF_EXHAUSTIVITY = stringPreferencesKey("exhaustivity")
        val PREF_EXPLANATION = stringPreferencesKey("explanation")

        val TRUSTED_CONTACTS_JSON = stringPreferencesKey("trusted_contacts_json")
    }

    suspend fun saveUserSession(
        userId: String,
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
            prefs[USER_ID] = userId
            prefs[SESSION_TOKEN] = sessionToken
            prefs[TOKENS] = tokens

            prefs[FULL_NAME] = fullName
            prefs[PHONE_NUMBER] = contact.phoneNumber
            prefs[EMAIL] = contact.email

            prefs[PREF_FONT_SIZE] = preferences.fontSize.name
            prefs[PREF_SOUND] = preferences.notificationSound.name
            prefs[PREF_COLOR] = preferences.colorScheme.name
            prefs[PREF_EXHAUSTIVITY] = preferences.exhaustivity.name
            prefs[PREF_EXPLANATION] = preferences.explanationMode.name

            prefs[TRUSTED_CONTACTS_JSON] = trustedContactsJson
        }
    }

    suspend fun updateSettings(
        fontSize: AppFontSize,
        sound: AppNotificationSound,
        color: AppColorScheme,
        exhaustivity: AppExhaustivity,
        explanation: AppExplanationMode
    ) {
        context.dataStore.edit { prefs ->
            prefs[PREF_FONT_SIZE] = fontSize.name
            prefs[PREF_SOUND] = sound.name
            prefs[PREF_COLOR] = color.name
            prefs[PREF_EXHAUSTIVITY] = exhaustivity.name
            prefs[PREF_EXPLANATION] = explanation.name
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

    val userData: Flow<UserData> = context.dataStore.data.map { prefs ->

        val contact = Contact(
            phoneNumber = prefs[PHONE_NUMBER] ?: "",
            email = prefs[EMAIL] ?: ""
        )

        val preferences = AppPreferences(
            fontSize = enumValueOfOrNull<AppFontSize>(prefs[PREF_FONT_SIZE]) ?: AppFontSize.REGULAR,
            notificationSound = enumValueOfOrNull<AppNotificationSound>(prefs[PREF_SOUND]) ?: AppNotificationSound.ON,
            colorScheme = enumValueOfOrNull<AppColorScheme>(prefs[PREF_COLOR]) ?: AppColorScheme.STANDARD,
            exhaustivity = enumValueOfOrNull<AppExhaustivity>(prefs[PREF_EXHAUSTIVITY]) ?: AppExhaustivity.REGULAR,
            explanationMode = enumValueOfOrNull<AppExplanationMode>(prefs[PREF_EXPLANATION]) ?: AppExplanationMode.ON
        )

        val jsonTrustedContacts = prefs[TRUSTED_CONTACTS_JSON] ?: "[]"
        val typeToken = object : TypeToken<List<TrustedContact>>() {}.type
        val trustedContacts: List<TrustedContact> = try {
            gson.fromJson(jsonTrustedContacts, typeToken)
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
        return try {
            if (name != null) enumValueOf<T>(name) else null
        } catch (e: Exception) { null }
    }
}