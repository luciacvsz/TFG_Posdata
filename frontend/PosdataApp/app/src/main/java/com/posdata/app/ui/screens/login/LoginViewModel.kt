package com.posdata.app.ui.screens.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.LoginRepository
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
 * Exposes [state] as a Compose-observable property so the UI recomposes
 * automatically on every state change.
 *
 * @param repository Repository responsible for the login and sync flow.
 */
class LoginViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

    /**
     * Initiates the login flow with the given credentials.
     *
     * Validates that neither field is blank before proceeding.
     * Updates [state] to reflect the loading, success, or failure outcome.
     *
     * @param email Email address entered by the user.
     * @param password Plain-text password entered by the user.
     */
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()){
            state = state.copy(errorMessage = "Por favor, rellene todos los campos")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLogging = true, errorMessage = null)

            val result = repository.performLoginAndSync(email, password)

            state = result.fold(
                onSuccess = { state.copy(isLogging = false, isSuccess = true) },
                onFailure = { error -> state.copy(isLogging = false, errorMessage = error.message) }
            )
        }
    }

    /**
     * Clears the current error message, dismissing the error dialog.
     */
    fun dismissError() {
        state = state.copy(errorMessage = null)
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

            val repository = LoginRepository(context, localApi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}