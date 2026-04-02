package com.posdata.app.ui.screens.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.LoginRepository
import com.posdata.app.data.repository.TokenConsumptionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state of the login screen.
 *
 * @param isLogging Whether a login operation is currently in progress.
 * @param errorMessage Error message to display in the error dialog, or null if there is no error.
 * @param isSuccess Whether the login completed successfully.
 */
data class LoginState(
    val isLogging: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

/**
 * ViewModel for the login screen.
 *
 * Manages the [LoginState] and delegates the login operation to [LoginRepository].
 * Exposes [uiState] as a [StateFlow] so the UI can collect it and recompose
 * automatically on every state change.
 *
 * @param repository Repository responsible for the login and sync flow.
 */
class LoginViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState: StateFlow<LoginState> = _uiState.asStateFlow()

    /**
     * Initiates the login flow with the given credentials.
     *
     * Validates that neither field is blank before proceeding.
     * Updates [uiState] to reflect the loading, success, or failure outcome.
     *
     * @param email Email address entered by the user.
     * @param password Plain-text password entered by the user.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Por favor, rellena todos los campos")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLogging = true, errorMessage = null)

            val result = repository.performLoginAndSync(email, password)

            _uiState.value = result.fold(
                onSuccess = { _uiState.value.copy(isLogging = false, isSuccess = true) },
                onFailure = { error -> _uiState.value.copy(isLogging = false, errorMessage = error.message) }
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
 * Factory for [LoginViewModel].
 *
 * Manually constructs all required dependencies since the project does not
 * use a dependency injection framework. Should be instantiated with the
 * application context to avoid memory leaks.
 *
 * @param context Application context used to initialize [UserDataStore] and [LoginRepository].
 */
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {

            val userInfo = UserDataStore(context)
            val localApi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance
            val tokenConsumptionRepository = TokenConsumptionRepository(userInfo, localApi)

            val repository = LoginRepository(localApi, cloudApi, userInfo, tokenConsumptionRepository)

            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}