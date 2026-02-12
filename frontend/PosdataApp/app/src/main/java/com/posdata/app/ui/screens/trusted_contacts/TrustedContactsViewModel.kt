package com.posdata.app.ui.screens.trusted_contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.UserRepository
import com.posdata.app.model.TrustedContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Estado de la UI
sealed class ContactsUiState {
    object Idle : ContactsUiState()
    object Loading : ContactsUiState()
    object Success : ContactsUiState()
    data class Error(val message: String) : ContactsUiState()
}

class TrustedContactsViewModel(private val repository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Idle)
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    fun addContact(currentList: List<TrustedContact>, newContact: TrustedContact) {
        val newList = currentList + newContact
        syncContacts(newList)
    }

    fun updateContact(currentList: List<TrustedContact>, index: Int, updatedContact: TrustedContact) {
        if (index in currentList.indices) {
            val newList = currentList.toMutableList()
            newList[index] = updatedContact
            syncContacts(newList)
        }
    }

    fun deleteContact(currentList: List<TrustedContact>, index: Int) {
        if (index in currentList.indices) {
            val newList = currentList.toMutableList()
            newList.removeAt(index)
            syncContacts(newList)
        }
    }

    private fun syncContacts(newList: List<TrustedContact>) {
        viewModelScope.launch {
            _uiState.value = ContactsUiState.Loading

            val result = repository.syncContacts(newList)

            result.onSuccess {
                _uiState.value = ContactsUiState.Success
            }.onFailure { error ->
                _uiState.value = ContactsUiState.Error(error.message ?: "Error al sincronizar contactos")
            }
        }
    }

    fun resetState() {
        _uiState.value = ContactsUiState.Idle
    }
}

class TrustedContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrustedContactsViewModel::class.java)) {
            val userInfo = UserInfo(context)

            val localAPi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance

            val repository = UserRepository(localAPi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return TrustedContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}