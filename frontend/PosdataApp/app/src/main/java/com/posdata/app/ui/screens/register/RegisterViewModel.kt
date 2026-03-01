package com.posdata.app.ui.screens.register

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.RegisterRepository
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
 * Exposes [state] as a Compose-observable property so the UI recomposes
 * automatically on every state change.
 *
 * @param repository Repository responsible for the full registration flow.
 */
class RegisterViewModel(
    private val repository: RegisterRepository
) : ViewModel() {
    var state by mutableStateOf(RegisterState())
        private set

    /**
     * Initiates the registration flow with the given user details.
     *
     * Validates that no field is blank before proceeding.
     * Updates [state] to reflect the loading, success, or failure outcome.
     *
     * @param fullName Full name entered by the user.
     * @param phoneNumber Phone number entered by the user.
     * @param email Email address entered by the user.
     * @param password Plain-text password entered by the user.
     */
    fun register(fullName: String, phoneNumber: String, email: String, password: String) {
        if(fullName.isBlank() || email.isBlank() || phoneNumber.isBlank() || password.isBlank()) {
            state = state.copy(errorMessage = "Por favor, rellene todos los campos")
            return
        }

        viewModelScope.launch {
            state = state.copy(isRegistering = true, errorMessage = null)

            val result = repository.performRegistration(fullName, phoneNumber, email, password)

            state = result.fold(
                onSuccess = { state.copy(isRegistering = false, isSuccess = true) },
                onFailure = { error ->
                    state.copy(
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
        state = state.copy(errorMessage = null)
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