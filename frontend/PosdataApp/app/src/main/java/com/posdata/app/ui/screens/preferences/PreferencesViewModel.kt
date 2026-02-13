package com.posdata.app.ui.screens.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.UserUpdateRepository
import com.posdata.app.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class PreferencesUiState {
    object Idle : PreferencesUiState()
    object Loading : PreferencesUiState()
    data class Success(val message: String) : PreferencesUiState()
    data class Error(val message: String) : PreferencesUiState()
}

class PreferencesViewModel(
    private val repository: UserUpdateRepository,
    private val userInfo: UserInfo
) : ViewModel() {

    private val _uiState = MutableStateFlow<PreferencesUiState>(PreferencesUiState.Idle)
    val uiState: StateFlow<PreferencesUiState> = _uiState.asStateFlow()

    val currentColorScheme: StateFlow<AppColorScheme> = userInfo.userData
        .map { it.preferences.colorScheme }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppColorScheme.LIGHT
        )

    val currentFontSize: StateFlow<AppFontSize> = userInfo.userData
        .map { it.preferences.fontSize }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppFontSize.REGULAR
        )

    fun updateColorScheme(colorScheme: AppColorScheme) {
        viewModelScope.launch {
            _uiState.value = PreferencesUiState.Loading

            val result = repository.updatePreferences(colorScheme = colorScheme)

            result.onSuccess {
                _uiState.value = PreferencesUiState.Success("¡Cambio guardado correctamente!")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }.onFailure { error ->
                _uiState.value = PreferencesUiState.Error(error.message ?: "Error al actualizar paleta de colores")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }
        }
    }

    fun updateFontSize(isChecked: Boolean) {
        viewModelScope.launch {
            _uiState.value = PreferencesUiState.Loading

            val fontSize = if (isChecked) AppFontSize.LARGE else AppFontSize.REGULAR

            val result = repository.updatePreferences(fontSize = fontSize)

            result.onSuccess {
                _uiState.value = PreferencesUiState.Success("¡Cambio guardado correctamente!")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }.onFailure { error ->
                _uiState.value = PreferencesUiState.Error(error.message ?: "Error al actualizar tamaño de fuente")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }
        }
    }

    fun updateNotificationSound(isChecked: Boolean) {
        viewModelScope.launch {
            _uiState.value = PreferencesUiState.Loading

            val notificationSound = if (isChecked) AppNotificationSound.ON else AppNotificationSound.OFF

            val result = repository.updatePreferences(notificationSound = notificationSound)

            result.onSuccess {
                _uiState.value = PreferencesUiState.Success("¡Cambio guardado correctamente!")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }.onFailure { error ->
                _uiState.value = PreferencesUiState.Error(error.message ?: "Error al actualizar sonido de notificación")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }
        }
    }

    fun updateExhaustivity(isChecked: Boolean) {
        viewModelScope.launch {
            _uiState.value = PreferencesUiState.Loading

            val exhaustivity = if (isChecked) AppExhaustivity.ENHANCED else AppExhaustivity.REGULAR

            val result = repository.updatePreferences(exhaustivity = exhaustivity)

            result.onSuccess {
                _uiState.value = PreferencesUiState.Success("¡Cambio guardado correctamente!")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }.onFailure { error ->
                _uiState.value = PreferencesUiState.Error(error.message ?: "Error al actualizar nivel de exhaustividad")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }
        }
    }

    fun updateExplanationMode(isChecked: Boolean) {
        viewModelScope.launch {
            _uiState.value = PreferencesUiState.Loading

            val explanationMode = if (isChecked) AppExplanationMode.ON else AppExplanationMode.OFF

            val result = repository.updatePreferences(explanationMode = explanationMode)

            result.onSuccess {
                _uiState.value = PreferencesUiState.Success("¡Cambio guardado correctamente!")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }.onFailure { error ->
                _uiState.value = PreferencesUiState.Error(error.message ?: "Error al actualizar explicaciones")
                delay(2000)
                _uiState.value = PreferencesUiState.Idle
            }
        }
    }

    fun resetState() {
        _uiState.value = PreferencesUiState.Idle
    }
}

class PreferencesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {

            val userInfo = UserInfo(context)
            val localAPi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance
            val repository = UserUpdateRepository(localAPi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return PreferencesViewModel(repository, userInfo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}