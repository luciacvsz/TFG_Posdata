package com.posdata.app.ui.screens.register

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.RegisterRepository
import com.posdata.app.ui.screens.login.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state of the registration screen.
 *
 * @param isRegistering Whether a registration operation is currently in progress.
 * @param errorMessage Error message to display in the error dialog, or null if there is no error.
 * @param isSuccess Whether the registration completed successfully.
 */
data class RegisterState(
    val isRegistering: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

/**
 * ViewModel for the registration screen.
 *
 * Manages the [RegisterState] and delegates the registration operation to [RegisterRepository].
 * Exposes [uiState] as a [StateFlow] so the UI can collect it and recompose
 * automatically on every state change.
 *
 * @param repository Repository responsible for the full registration flow.
 */
class RegisterViewModel(
    private val repository: RegisterRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterState())
    val uiState: StateFlow<RegisterState> = _uiState.asStateFlow()

    /**
     * Initiates the registration flow with the given user details.
     *
     * Validates that no field is blank before proceeding.
     * Updates [uiState] to reflect the loading, success, or failure outcome.
     *
     * @param fullName Full name entered by the user.
     * @param phoneNumber Phone number entered by the user.
     * @param email Email address entered by the user.
     * @param password Plain-text password entered by the user.
     */
    fun register(fullName: String, phoneNumber: String, email: String, password: String) {
        if(fullName.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor, rellene todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRegistering = true, errorMessage = null)

            val result = repository.performRegistration(fullName, phoneNumber, email, password)

            _uiState.value = result.fold(
                onSuccess = { _uiState.value.copy(isRegistering = false, isSuccess = true) },
                onFailure = { error ->
                    _uiState.value.copy(
                        isRegistering = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    /**
     * Clears the current error message, dismissing the error dialog.
     */
    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * Factory for [RegisterViewModel].
 *
 * Manually constructs all required dependencies since the project does not
 * use a dependency injection framework. Should be instantiated with the
 * application context to avoid memory leaks.
 *
 * @param context Application context used to initialize [UserDataStore] and [RegisterRepository].
 */
class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {

            val userInfo = UserDataStore(context)
            val localApi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance

            val repository = RegisterRepository(context, localApi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}