package com.posdata.app.ui.screens.trusted_contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.TokenConsumptionRepository
import com.posdata.app.data.repository.UserUpdateRepository
import com.posdata.app.model.TrustedContact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TrustedContactsUiState {
    object Idle : TrustedContactsUiState()
    object Loading : TrustedContactsUiState()
    data class Success(val message: String) : TrustedContactsUiState()
    data class Error(val message: String) : TrustedContactsUiState()
}

class TrustedContactsViewModel(
    private val repository: UserUpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrustedContactsUiState>(TrustedContactsUiState.Idle)
    val uiState: StateFlow<TrustedContactsUiState> = _uiState.asStateFlow()

    fun addContact(currentList: List<TrustedContact>, newContact: TrustedContact) {
        val newList = currentList + newContact
        syncContacts(newList, "Contacto añadido correctamente")
    }

    fun updateContact(currentList: List<TrustedContact>, index: Int, updatedContact: TrustedContact) {
        if (index in currentList.indices) {
            val newList = currentList.toMutableList()
            newList[index] = updatedContact
            syncContacts(newList, "Contacto actualizado")
        }
    }

    fun deleteContact(currentList: List<TrustedContact>, index: Int) {
        if (index in currentList.indices) {
            val newList = currentList.toMutableList()
            newList.removeAt(index)
            syncContacts(newList, "Contacto eliminado")
        }
    }

    private fun syncContacts(newList: List<TrustedContact>, successMessage: String) {
        viewModelScope.launch {
            _uiState.value = TrustedContactsUiState.Loading

            val result = repository.syncContacts(newList)

            result.onSuccess {
                _uiState.value = TrustedContactsUiState.Success(successMessage)
                delay(2000)
                _uiState.value = TrustedContactsUiState.Idle
            }.onFailure { error ->
                _uiState.value = TrustedContactsUiState.Error(error.message ?: "Error al sincronizar contactos")
                delay(2000)
                _uiState.value = TrustedContactsUiState.Idle
            }
        }
    }

    fun resetState() {
        _uiState.value = TrustedContactsUiState.Idle
    }
}

class TrustedContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrustedContactsViewModel::class.java)) {
            val userInfo = UserInfo(context)

            val localAPi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance
            val tokenConsumptionRepository = TokenConsumptionRepository(userInfo)

            val repository = UserUpdateRepository(localAPi, cloudApi, userInfo, tokenConsumptionRepository)

            @Suppress("UNCHECKED_CAST")
            return TrustedContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}