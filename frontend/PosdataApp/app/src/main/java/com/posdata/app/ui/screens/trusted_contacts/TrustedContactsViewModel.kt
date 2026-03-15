package com.posdata.app.ui.screens.trusted_contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.TokenConsumptionRepository
import com.posdata.app.data.repository.UserUpdateRepository
import com.posdata.app.model.TrustedContact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Represents the UI state of the trusted contacts screen.
 *
 * - [Idle]: No operation in progress.
 * - [Loading]: A sync operation is currently in progress.
 * - [Success]: The last operation completed successfully.
 * - [Error]: The last operation failed.
 */
sealed class TrustedContactsUiState {
    object Idle : TrustedContactsUiState()
    object Loading : TrustedContactsUiState()
    data class Success(val message: String) : TrustedContactsUiState()
    data class Error(val message: String) : TrustedContactsUiState()
}

/**
 * ViewModel for the trusted contacts screen.
 *
 * Manages add, update, and delete operations on the trusted contacts list.
 * All three operations work by modifying a local copy of the list and
 * syncing the full updated list to the cloud via [UserUpdateRepository.syncContacts].
 *
 * @param repository Repository responsible for syncing the contacts list.
 */
class TrustedContactsViewModel(
    private val repository: UserUpdateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrustedContactsUiState>(TrustedContactsUiState.Idle)
    val uiState: StateFlow<TrustedContactsUiState> = _uiState.asStateFlow()

    /**
     * Adds a new contact to the list and syncs the result to the cloud.
     *
     * @param currentList The current list of trusted contacts.
     * @param newContact The contact to add.
     */
    fun addContact(currentList: List<TrustedContact>, newContact: TrustedContact) {
        syncContacts(currentList + newContact, "Contacto añadido correctamente")
    }

    /**
     * Replaces the contact at the given index and syncs the result to the cloud.
     * Does nothing if [index] is out of bounds.
     *
     * @param currentList The current list of trusted contacts.
     * @param index Index of the contact to update.
     * @param updatedContact The new contact data to apply.
     */
    fun updateContact(currentList: List<TrustedContact>, index: Int, updatedContact: TrustedContact) {
        if (index in currentList.indices) {
            val newList = currentList.toMutableList()
            newList[index] = updatedContact
            syncContacts(newList, "Contacto actualizado")
        }
    }

    /**
     * Removes the contact at the given index and syncs the result to the cloud.
     * Does nothing if [index] is out of bounds.
     *
     * @param currentList The current list of trusted contacts.
     * @param index Index of the contact to remove.
     */
    fun deleteContact(currentList: List<TrustedContact>, index: Int) {
        if (index in currentList.indices) {
            val newList = currentList.toMutableList()
            newList.removeAt(index)
            syncContacts(newList, "Contacto eliminado")
        }
    }

    /**
     * Syncs the given contact list to the cloud and updates [_uiState] accordingly.
     *
     * @param newList The updated list to persist.
     * @param successMessage Message to display on success.
     */
    private fun syncContacts(newList: List<TrustedContact>, successMessage: String) {
        viewModelScope.launch {
            _uiState.value = TrustedContactsUiState.Loading

            val result = repository.syncContacts(newList)

            result.fold(
                onSuccess = {
                    _uiState.value = TrustedContactsUiState.Success(successMessage)
                },
                onFailure = { error ->
                    _uiState.value = TrustedContactsUiState.Error(
                        error.message ?: "Error al sincronizar contactos"
                    )
                }
            )

            delay(2000)
            _uiState.value = TrustedContactsUiState.Idle
        }
    }

    /**
     * Resets the UI state to [TrustedContactsUiState.Idle], dismissing any dialog.
     */
    fun resetState() {
        _uiState.value = TrustedContactsUiState.Idle
    }
}

/**
 * Factory for [TrustedContactsViewModel].
 *
 * Manually constructs all required dependencies since the project does not
 * use a dependency injection framework. Should be instantiated with the
 * application context to avoid memory leaks.
 *
 * @param context Application context used to initialize [UserDataStore].
 */
class TrustedContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrustedContactsViewModel::class.java)) {
            val userInfo = UserDataStore(context)

            val localApi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance
            val tokenConsumptionRepository = TokenConsumptionRepository(userInfo, localApi)

            val repository = UserUpdateRepository(localApi, cloudApi, userInfo, tokenConsumptionRepository)

            @Suppress("UNCHECKED_CAST")
            return TrustedContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}