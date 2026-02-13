package com.posdata.app.ui.screens.register

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.RegisterRepository
import kotlinx.coroutines.launch

data class RegisterState(
    val isRegistering: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
)

class RegisterViewModel(
    private val repository: RegisterRepository
) : ViewModel() {
    var state by mutableStateOf(RegisterState())
        private set

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

    fun dismissError() {
        state = state.copy(errorMessage = null)
    }
}

class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {

            val userInfo = UserInfo(context)

            val localAPi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance

            val repository = RegisterRepository(localAPi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}