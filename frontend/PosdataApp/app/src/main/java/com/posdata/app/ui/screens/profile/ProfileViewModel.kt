package com.posdata.app.ui.screens.profile

import android.content.Context
import android.media.tv.TvContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.LocalApiService
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.DeleteAccountRepository
import com.posdata.app.data.repository.LogoutRepository
import com.posdata.app.data.repository.UserUpdateRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val message: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

enum class ProfileField { NONE, FULL_NAME, PHONE_NUMBER, EMAIL, PASSWORD }

class ProfileViewModel(
    private val userUpdateRepository: UserUpdateRepository,
    private val logoutRepository: LogoutRepository,
    private val deleteAccountRepository: DeleteAccountRepository,
    private val userInfo: UserInfo
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateProfileField(
        field: ProfileField,
        newValue: String
    ) {
        if (newValue.isBlank()) return

        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val result: Result<Boolean> = when (field) {
                ProfileField.FULL_NAME -> userUpdateRepository.updateProfile(fullName = newValue)
                ProfileField.PHONE_NUMBER -> userUpdateRepository.updateProfile(phoneNumber = newValue)
                ProfileField.EMAIL -> userUpdateRepository.updateProfile(email = newValue)
                ProfileField.PASSWORD -> userUpdateRepository.updateProfile(password = newValue)
                else -> {}
            } as Result<Boolean>

            result.onSuccess {
                _uiState.value = ProfileUiState.Success("¡Cambio guardado correctamente!")
                delay(2000)
                _uiState.value = ProfileUiState.Idle
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "No se pudo guardar el cambio")
                delay(2000)
                _uiState.value = ProfileUiState.Idle
            }
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = logoutRepository.performLogout()
            result.onSuccess {
                _uiState.value = ProfileUiState.Success("Sesión cerrada")
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "Error al cerrar sesión")
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = deleteAccountRepository.performDeleteAccount()
            result.onSuccess {
                _uiState.value = ProfileUiState.Success("Cuenta eliminada correctamente")
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "No se pudo eliminar la cuenta")
            }
        }
    }
}

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val userInfo = UserInfo(context)

            val localApi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance

            val userUpdateRepository = UserUpdateRepository(localApi, cloudApi, userInfo)
            val logoutRepository = LogoutRepository(context, userInfo)
            val deleteAccountRepository = DeleteAccountRepository(localApi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userUpdateRepository, logoutRepository, deleteAccountRepository, userInfo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}