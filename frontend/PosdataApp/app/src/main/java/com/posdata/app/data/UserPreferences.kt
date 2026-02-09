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
        val USER_TOKENS = intPreferencesKey("user_tokens")
        val USER_FULL_NAME = stringPreferencesKey("user_name")
        val USER_PHONE_NUMBER = stringPreferencesKey("user_phone_number")
        val USER_EMAIL = stringPreferencesKey("user_email")

        val PREF_FONT_SIZE = stringPreferencesKey("font_size")
        val PREF_SOUND = stringPreferencesKey("notification_sound")
        val PREF_COLOR = stringPreferencesKey("color_scheme")
        val PREF_EXHAUSTIVITY = stringPreferencesKey("exhaustivity")
        val PREF_EXPLANATION = stringPreferencesKey("explanation")

        val TRUSTED_CONTACTS_JSON = stringPreferencesKey("trusted_contacts_json")
    }

    suspend fun saveUserSession(
        userId: String,
        userFullName: String,
        userSessionToken: String,
        userTokens: Int,
        userContact: Contact,
        userPreferences: AppPreferences,
        userContacts: List<TrustedContact>
    ) {
        val contactsJson = gson.toJson(contacts)

        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userId
            prefs[USER_FULL_NAME] = userFullName
            prefs[USER_PHONE_NUMBER] = userPhoneNumber
            prefs[USER_EMAIL] = userEmail
            prefs[SESSION_TOKEN] = apiToken
            prefs[USER_TOKENS] = tokens

            prefs[TRUSTED_CONTACTS_JSON] = contactsJson

            prefs[PREF_FONT_SIZE] = preferences.fontSize.name
            prefs[PREF_SOUND] = preferences.notificationSound.name
            prefs[PREF_COLOR] = preferences.colorScheme.name
            prefs[PREF_EXHAUSTIVITY] = preferences.exhaustivity.name
            prefs[PREF_EXPLANATION] = preferences.explanationMode.name
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
            val current = prefs[USER_TOKENS] ?: 0
            if (current > 0) {
                prefs[USER_TOKENS] = current - 1
                success = true
            }
        }
        return success
    }

    val userData: Flow<UserData> = context.dataStore.data.map { prefs ->

        val jsonContacts = prefs[TRUSTED_CONTACTS_JSON] ?: "[]"
        val typeToken = object : TypeToken<List<TrustedContact>>() {}.type
        val contactsList: List<TrustedContact> = try {
            gson.fromJson(jsonContacts, typeToken)
        } catch (e: Exception) { emptyList() }

        val currentPreferences = AppPreferences(
            fontSize = enumValueOfOrNull<AppFontSize>(prefs[PREF_FONT_SIZE]) ?: AppFontSize.REGULAR,
            notificationSound = enumValueOfOrNull<AppNotificationSound>(prefs[PREF_SOUND]) ?: AppNotificationSound.ON,
            colorScheme = enumValueOfOrNull<AppColorScheme>(prefs[PREF_COLOR]) ?: AppColorScheme.STANDARD,
            exhaustivity = enumValueOfOrNull<AppExhaustivity>(prefs[PREF_EXHAUSTIVITY]) ?: AppExhaustivity.REGULAR,
            explanationMode = enumValueOfOrNull<AppExplanationMode>(prefs[PREF_EXPLANATION]) ?: AppExplanationMode.ON
        )

        UserData(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            userId = prefs[USER_ID] ?: "",
            userName = prefs[USER_NAME] ?: "",
            email = prefs[USER_EMAIL] ?: "",
            phone = prefs[USER_PHONE] ?: "",
            apiToken = prefs[SESSION_TOKEN] ?: "",
            tokens = prefs[USER_TOKENS] ?: 0,
            trustedContacts = contactsList,
            preferences = currentPreferences
        )
    }

    private inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
        return try {
            if (name != null) enumValueOf<T>(name) else null
        } catch (e: Exception) { null }
    }
}