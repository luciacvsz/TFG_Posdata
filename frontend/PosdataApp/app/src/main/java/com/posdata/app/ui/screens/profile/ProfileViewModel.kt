package com.posdata.app.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.DeleteAccountRepository
import com.posdata.app.data.repository.LogoutRepository
import com.posdata.app.data.repository.TokenConsumptionRepository
import com.posdata.app.data.repository.UserUpdateRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state of the profile screen.
 *
 * - [Idle]: No operation in progress.
 * - [Loading]: An operation is currently in progress.
 * - [Success]: The last operation completed successfully.
 * - [Error]: The last operation failed.
 */
sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val message: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

/**
 * Identifies which profile field is currently being edited.
 */
enum class ProfileField { NONE, FULL_NAME, PHONE_NUMBER, EMAIL, PASSWORD }

/**
 * ViewModel for the profile screen.
 *
 * Manages profile field updates, logout, and account deletion.
 * Navigation after logout or account deletion is handled automatically
 * by [com.posdata.app.MainActivity], which observes [UserDataStore.userData] and redirects
 * to the login screen when [isLoggedIn] becomes false.
 *
 * @param userUpdateRepository Repository responsible for profile and credential updates.
 * @param logoutRepository Repository responsible for the logout flow.
 * @param deleteAccountRepository Repository responsible for the account deletion flow.
 */
class ProfileViewModel(
    private val userUpdateRepository: UserUpdateRepository,
    private val logoutRepository: LogoutRepository,
    private val deleteAccountRepository: DeleteAccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    /**
     * Updates a single profile field with the given value.
     *
     * Routes the update to [UserUpdateRepository.updateProfile]
     * Does nothing if [newValue] is blank.
     *
     * @param field The profile field to update.
     * @param newValue The new value to apply.
     */
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
                ProfileField.NONE -> return@launch
            }

            result.fold(
                onSuccess = {
                    _uiState.value = ProfileUiState.Success("¡Cambio guardado correctamente!")
                    delay(2000)
                    _uiState.value = ProfileUiState.Idle
                },
                onFailure = { error ->
                    _uiState.value = ProfileUiState.Error(error.message ?: "No se pudo guardar el cambio")
                    delay(2000)
                    _uiState.value = ProfileUiState.Idle
                }
            )
        }
    }

    /**
     * Resets the UI state to [ProfileUiState.Idle], dismissing any dialog.
     */
    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }

    /**
     * Executes the logout flow.
     *
     * On success, [UserDataStore.userData] emits a new value with [isLoggedIn] = false,
     * which triggers automatic navigation to the login screen in [com.posdata.app.MainActivity].
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = logoutRepository.performLogout()
            result.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "Error al cerrar sesión")
            }
        }
    }

    /**
     * Executes the account deletion flow.
     *
     * On success, [UserDataStore.userData] emits a new value with [isLoggedIn] = false,
     * which triggers automatic navigation to the login screen in [com.posdata.app.MainActivity].
     */
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            val result = deleteAccountRepository.performDeleteAccount()
            result.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "No se pudo eliminar la cuenta")
            }
        }
    }
}

/**
 * Factory for [ProfileViewModel].
 *
 * Manually constructs all required dependencies since the project does not
 * use a dependency injection framework. Should be instantiated with the
 * application context to avoid memory leaks.
 *
 * @param context Application context used to initialize [UserDataStore].
 */
class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {

            val userInfo = UserDataStore(context)
            val localApi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance
            val tokenConsumptionRepository = TokenConsumptionRepository(userInfo)

            val userUpdateRepository = UserUpdateRepository(localApi, cloudApi, userInfo, tokenConsumptionRepository)
            val logoutRepository = LogoutRepository(context, userInfo)
            val deleteAccountRepository = DeleteAccountRepository(localApi, cloudApi, userInfo, tokenConsumptionRepository)

            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userUpdateRepository, logoutRepository, deleteAccountRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}