package com.posdata.app.ui.screens.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    object Success : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(private val repository: UserRepository, private val userInfo: UserInfo) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun updateProfile(
        newName: String? = null,
        newPhone: String? = null,
        newEmail: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading

            val result = repository.updateProfile(
                fullName = newName,
                phoneNumber = newPhone,
                email = newEmail
            )

            result.onSuccess {
                _uiState.value = ProfileUiState.Success
            }.onFailure { error ->
                _uiState.value = ProfileUiState.Error(error.message ?: "Error desconocido")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userInfo.clearSession()
        }
    }

    fun resetState() {
        _uiState.value = ProfileUiState.Idle
    }
}

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            val userInfo = UserInfo(context)

            val localAPi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance

            val repository = UserRepository(localAPi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(repository, userInfo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}