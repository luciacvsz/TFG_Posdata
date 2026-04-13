package com.posdata.app.ui.screens.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.TokenConsumptionRepository
import com.posdata.app.data.repository.UserUpdateRepository
import com.posdata.app.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Represents the UI state of the preferences screen.
 *
 * - [Idle]: No operation in progress.
 * - [Loading]: An update operation is currently in progress.
 * - [Success]: The last operation completed successfully.
 * - [Error]: The last operation failed.
 */
sealed class PreferencesUiState {
    object Idle : PreferencesUiState()
    object Loading : PreferencesUiState()
    data class Success(val message: String) : PreferencesUiState()
    data class Error(val message: String) : PreferencesUiState()
}

/**
 * ViewModel for the preferences screen.
 *
 * Exposes individual update functions for each preference field, all delegating
 * to [UserUpdateRepository.updatePreferences]. Each operation follows the same
 * pattern: set Loading → call repository → set Success or Error → reset to Idle.
 *
 * @param repository Repository responsible for persisting preference updates.
 * @param userInfo Local data source used to observe color scheme and font size preferences
 */
class PreferencesViewModel(
    private val repository: UserUpdateRepository,
    private val userInfo: UserDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreferencesUiState>(PreferencesUiState.Idle)
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    /**
     * Reactive stream of the user's current color scheme preference.
     *
     * Observed by [com.posdata.app.MainActivity] to apply the correct theme across the entire app.
     * Emits a new value whenever the user changes the color scheme in the preferences screen.
     */
    val currentColorScheme: StateFlow<AppColorScheme> = userInfo.userData
        .map { it.preferences.colorScheme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppColorScheme.LIGHT
        )

    /**
     * Reactive stream of the user's current font size preference.
     *
     * Observed by [com.posdata.app.MainActivity] to apply the correct typography scale across the entire app.
     * Emits a new value whenever the user changes the font size in the preferences screen.
     */
    val currentFontSize: StateFlow<AppFontSize> = userInfo.userData
        .map { it.preferences.fontSize }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppFontSize.REGULAR
        )

    /**
     * Updates the application color scheme.
     *
     * @param colorScheme The new color scheme to apply.
     */
    fun updateColorScheme(colorScheme: AppColorScheme) {
        performUpdate(errorFallback = "Error al actualizar paleta de colores") {
            repository.updatePreferences(colorScheme = colorScheme)
        }
    }

    /**
     * Updates the font size preference.
     *
     * @param isChecked True for [AppFontSize.LARGE], false for [AppFontSize.REGULAR].
     */
    fun updateFontSize(isChecked: Boolean) {
        val fontSize = if (isChecked) AppFontSize.LARGE else AppFontSize.REGULAR
        performUpdate(errorFallback = "Error al actualizar tamaño de fuente") {
            repository.updatePreferences(fontSize = fontSize)
        }
    }

    /**
     * Updates the notification sound preference.
     *
     * @param isChecked True for [AppNotificationSound.ON], false for [AppNotificationSound.OFF].
     */
    fun updateNotificationSound(isChecked: Boolean) {
        val sound = if (isChecked) AppNotificationSound.ON else AppNotificationSound.OFF
        performUpdate(errorFallback = "Error al actualizar sonido de notificación") {
            repository.updatePreferences(notificationSound = sound)
        }
    }

    /**
     * Updates the analysis exhaustivity level.
     *
     * @param isChecked True for [AppExhaustivity.ENHANCED], false for [AppExhaustivity.REGULAR].
     */
    fun updateExhaustivity(isChecked: Boolean) {
        val exhaustivity = if (isChecked) AppExhaustivity.ENHANCED else AppExhaustivity.REGULAR
        performUpdate(errorFallback = "Error al actualizar nivel de exhaustividad") {
            repository.updatePreferences(exhaustivity = exhaustivity)
        }
    }

    /**
     * Updates the explanation mode preference.
     *
     * @param isChecked True for [AppExplanationMode.ON], false for [AppExplanationMode.OFF].
     */
    fun updateExplanationMode(isChecked: Boolean) {
        val mode = if (isChecked) AppExplanationMode.ON else AppExplanationMode.OFF
        performUpdate(errorFallback = "Error al actualizar explicaciones") {
            repository.updatePreferences(explanationMode = mode)
        }
    }

    /**
     * Resets the UI state to [PreferencesUiState.Idle], dismissing any dialog.
     */
    fun resetState() {
        _uiState.value = PreferencesUiState.Idle
    }

    /**
     * Executes a preference update operation and manages the [_uiState] transitions.
     *
     * Sets [PreferencesUiState.Loading] before the call, then transitions to
     * [PreferencesUiState.Success] or [PreferencesUiState.Error] based on the result,
     * and resets to [PreferencesUiState.Idle] after a short display delay.
     *
     * @param errorFallback Fallback error message used if the exception has no message.
     * @param block Suspend lambda that performs the actual repository call.
     */
    private fun performUpdate(
        errorFallback: String,
        block: suspend () -> Result<Boolean>
    ) {
        viewModelScope.launch {
            _uiState.value = PreferencesUiState.Loading

            val result = block()

            result.fold(
                onSuccess = {
                    _uiState.value = PreferencesUiState.Success("¡Cambio guardado correctamente!")
                },
                onFailure = { error ->
                    _uiState.value = PreferencesUiState.Error(error.message ?: errorFallback)
                }
            )

            delay(2000)
            _uiState.value = PreferencesUiState.Idle
        }
    }
}

/**
 * Factory for [PreferencesViewModel].
 *
 * Manually constructs all required dependencies since the project does not
 * use a dependency injection framework. Should be instantiated with the
 * application context to avoid memory leaks.
 *
 * Passes [UserDataStore] both to [UserUpdateRepository] and directly to
 * [PreferencesViewModel], which uses it to expose [currentColorScheme]
 * and [currentFontSize] to [com.posdata.app.MainActivity] for app-wide theme application.
 *
 * @param context Application context used to initialize [UserDataStore].
 */
class PreferencesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {

            val userInfo = UserDataStore(context)
            val localApi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance
            val tokenConsumptionRepository = TokenConsumptionRepository(userInfo, localApi)

            val repository = UserUpdateRepository(localApi, cloudApi, userInfo, tokenConsumptionRepository)

            @Suppress("UNCHECKED_CAST")
            return PreferencesViewModel(repository, userInfo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}