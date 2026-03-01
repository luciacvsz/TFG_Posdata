package com.posdata.app.ui.screens.trusted_contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posdata.app.model.TrustedContact
import com.posdata.app.model.UserData
import com.posdata.app.ui.components.PosdataContactClickableCard
import com.posdata.app.ui.components.PosdataContactDialog
import com.posdata.app.ui.components.PosdataPrimaryButton
import com.posdata.app.ui.components.PosdataStatusDialog

/**
 * Trusted contacts screen content.
 *
 * Displays the user's list of trusted contacts as tappable cards that open
 * an edit/delete dialog. Provides an "Añadir contacto" button that opens
 * an add dialog. All changes are synced to the cloud immediately via
 * [TrustedContactsViewModel].
 *
 * Shows a fullscreen loading overlay while a sync is in progress,
 * and a status dialog on success or failure.
 *
 * @param userData Current session data used to read the current contacts list.
 * @param viewModel ViewModel managing contact sync operations and UI state.
 */
@Composable
fun TrustedContactsContent(
    userData: UserData?,
    viewModel: TrustedContactsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val trustedContacts = userData?.trustedContacts ?: emptyList()
    var contactToEdit by remember { mutableStateOf<TrustedContact?>(null) }
    var indexToEdit by remember { mutableStateOf(-1) }

    var showAddDialog by remember { mutableStateOf(false) }
    val isLoading = uiState is TrustedContactsUiState.Loading

    if (uiState is TrustedContactsUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is TrustedContactsUiState.Success) {
            showAddDialog = false
            contactToEdit = null
            indexToEdit = -1
        }
    }

    when (val state = uiState) {
        is TrustedContactsUiState.Success -> {
            PosdataStatusDialog(
                message = state.message,
                isSuccess = true,
                onDismiss = { viewModel.resetState() }
            )
        }
        is TrustedContactsUiState.Error -> {
            PosdataStatusDialog(
                message = state.message,
                isSuccess = false,
                onDismiss = { viewModel.resetState() }
            )
        }
        else -> {}
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Contactos de\nConfianza",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (trustedContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes contactos de confianza añadidos.\n¡Añade uno para estar más seguro!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    trustedContacts.forEachIndexed { index, contact ->
                        PosdataContactClickableCard(
                            name = contact.name,
                            role = contact.role,
                            phone = contact.phoneNumber,
                            email = contact.email,
                            onClick = {
                                indexToEdit = index
                                contactToEdit = contact
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                PosdataPrimaryButton(
                    text = "Añadir contacto",
                    onClick = { showAddDialog = true },
                    isLoading = isLoading
                )
            }
        }

        if (showAddDialog) {
            PosdataContactDialog(
                title = "Añadir Contacto",
                initialName = "",
                initialRole = "",
                initialPhone = "",
                initialEmail = "",
                onDismiss = { showAddDialog = false },
                onSave = { name, role, phone, email ->
                    val newContact = TrustedContact(
                        name = name,
                        role = role,
                        phoneNumber = phone.ifBlank { null },
                        email = email.ifBlank { null }
                    )

                    viewModel.addContact(trustedContacts, newContact)
                }
            )
        }

        if (contactToEdit != null && indexToEdit != -1) {
            PosdataContactDialog(
                title = "Editar Contacto",
                initialName = contactToEdit!!.name,
                initialRole = contactToEdit!!.role,
                initialPhone = contactToEdit!!.phoneNumber ?: "",
                initialEmail = contactToEdit!!.email ?: "",
                isEditMode = true,
                onDismiss = {
                    contactToEdit = null
                    indexToEdit = -1
                },
                onSave = { name, role, phone, email ->
                    val updatedContact = TrustedContact(
                        name = name,
                        role = role,
                        phoneNumber = phone.ifBlank { null },
                        email = email.ifBlank { null }
                    )

                    viewModel.updateContact(trustedContacts, indexToEdit, updatedContact)
                },
                onDelete = {
                    viewModel.deleteContact(trustedContacts, indexToEdit)
                }
            )
        }
    }
}