package com.posdata.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "posdata_settings")

class UserPreferences(private val context: Context) {

    private val gson = Gson()

    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_PHONE = stringPreferencesKey("user_phone")
        val USER_TOKENS = intPreferencesKey("user_tokens")
        val TRUSTED_CONTACTS_JSON = stringPreferencesKey("trusted_contacts_json")
        val SETTING_FONT_SIZE = stringPreferencesKey("setting_font_size")
        val SETTING_NOTIF_SOUND = stringPreferencesKey("setting_notif_sound")
        val SETTING_COLOR_SCHEME = stringPreferencesKey("setting_color_scheme")
        val SETTING_EXHAUSTIVITY = stringPreferencesKey("setting_exhaustivity")
        val SETTING_EXPLANATION = stringPreferencesKey("setting_explanation")
    }

    suspend fun saveUserSession(
        id: String,
        name: String,
        email: String,
        phone: String,
        token: String,
        tokens: Int,
        contacts: List<TrustedContact>
    ) {
        val contactsJson = gson.toJson(contacts)

        context.dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = id
            prefs[USER_NAME] = name
            prefs[USER_EMAIL] = email
            prefs[USER_PHONE] = phone
            prefs[USER_TOKENS] = tokens
            prefs[TRUSTED_CONTACTS_JSON] = contactsJson

            if (prefs[SETTING_FONT_SIZE] == null) prefs[SETTING_FONT_SIZE] = AppFontSize.REGULAR.name
            if (prefs[SETTING_NOTIF_SOUND] == null) prefs[SETTING_NOTIF_SOUND] = AppNotificationSound.ON.name
            if (prefs[SETTING_COLOR_SCHEME] == null) prefs[SETTING_COLOR_SCHEME] = AppColorScheme.STANDARD.name
        }
    }

    // 2. ACTUALIZAR SOLO PREFERENCIAS (Desde pantalla Ajustes)
    suspend fun updateSettings(
        fontSize: AppFontSize,
        sound: AppNotificationSound,
        color: AppColorScheme,
        exhaustivity: AppExhaustivity,
        explanation: AppExplanationMode,
        alert: AppExtraAlert
    ) {
        context.dataStore.edit { prefs ->
            prefs[SETTING_FONT_SIZE] = fontSize.name
            prefs[SETTING_NOTIF_SOUND] = sound.name
            prefs[SETTING_COLOR_SCHEME] = color.name
            prefs[SETTING_EXHAUSTIVITY] = exhaustivity.name
            prefs[SETTING_EXPLANATION] = explanation.name
            prefs[SETTING_EXTRA_ALERT] = alert.name
        }
    }

    // 3. RESTAR TOKEN
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

    // 4. CERRAR SESIÓN
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }

    // 5. LEER DATOS (Flujo constante)
    val userData: Flow<UserData> = context.dataStore.data.map { prefs ->

        // Deserializar contactos
        val jsonContacts = prefs[TRUSTED_CONTACTS_JSON] ?: "[]"
        val typeToken = object : TypeToken<List<TrustedContact>>() {}.type
        val contactsList: List<TrustedContact> = try {
            gson.fromJson(jsonContacts, typeToken)
        } catch (e: Exception) { emptyList() }

        UserData(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            userId = prefs[USER_ID] ?: "",
            userName = prefs[USER_NAME] ?: "",
            email = prefs[USER_EMAIL] ?: "",
            phone = prefs[USER_PHONE] ?: "",
            tokens = prefs[USER_TOKENS] ?: 0,
            trustedContacts = contactsList,

            fontSize = enumValueOfOrNull(prefs[SETTING_FONT_SIZE]) ?: AppFontSize.REGULAR,
            notifSound = enumValueOfOrNull(prefs[SETTING_NOTIF_SOUND]) ?: AppNotificationSound.ON,
            colorScheme = enumValueOfOrNull(prefs[SETTING_COLOR_SCHEME]) ?: AppColorScheme.STANDARD,
            exhaustivity = enumValueOfOrNull(prefs[SETTING_EXHAUSTIVITY]) ?: AppExhaustivity.REGULAR,
            explanationMode = enumValueOfOrNull(prefs[SETTING_EXPLANATION]) ?: AppExplanationMode.ON,
            extraAlert = enumValueOfOrNull(prefs[SETTING_EXTRA_ALERT]) ?: AppExtraAlert.OFF
        )
    }

    // Helper para evitar crasheos si el enum guardado no existe
    private inline fun <reified T : Enum<T>> enumValueOfOrNull(name: String?): T? {
        return try {
            if (name != null) enumValueOf<T>(name) else null
        } catch (e: Exception) { null }
    }
}

// --- CLASE CONTENEDORA FINAL ---
data class UserData(
    val isLoggedIn: Boolean,
    val userId: String, // ¡Importantísimo!
    val userName: String,
    val email: String,
    val phone: String,
    val tokens: Int,
    val trustedContacts: List<TrustedContact>,

    // Preferencias
    val fontSize: AppFontSize,
    val notifSound: AppNotificationSound,
    val colorScheme: AppColorScheme,
    val exhaustivity: AppExhaustivity,
    val explanationMode: AppExplanationMode,
    val extraAlert: AppExtraAlert
)