package com.posdata.app.ui.screens.login

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.LoginRepository
import kotlinx.coroutines.launch

data class LoginState(
    val isLogging: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class LoginViewModel(
    private val repository: LoginRepository
) : ViewModel() {

    var state by mutableStateOf(LoginState())
        private set

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

    fun dismissError() {
        state = state.copy(errorMessage = null)
    }
}

class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {

            val userInfo = UserInfo(context)

            val localAPi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance

            val repository = LoginRepository(localAPi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}