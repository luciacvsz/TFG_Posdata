package com.posdata.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posdata.app.model.UserData
import com.posdata.app.ui.components.PosdataClickableCard
import com.posdata.app.ui.components.PosdataPrimaryButton
import com.posdata.app.ui.theme.*

// Enumerado para la lógica de edición
enum class ProfileField { NONE, NAME, EMAIL, PHONE, PASSWORD }

@Composable
fun ProfileContent(
    userData: UserData?,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(LocalContext.current)
    )
) {
    // 1. Observación del estado del ViewModel
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 2. Estados locales para la navegación de diálogos
    var showEditDialog by remember { mutableStateOf(false) }
    var currentField by remember { mutableStateOf(ProfileField.NONE) }

    val isLoading = uiState is ProfileUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineLarge,
            color = PosdataBlackText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECCIÓN DE DATOS (Cards Estandarizadas) ---

        PosdataClickableCard(
            title = "Nombre",
            subtitle = userData?.fullName ?: "Cargando...",
            onClick = {
                currentField = ProfileField.NAME
                showEditDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PosdataClickableCard(
            title = "Correo Electrónico",
            subtitle = userData?.contact?.email ?: "Sin email",
            onClick = {
                currentField = ProfileField.EMAIL
                showEditDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PosdataClickableCard(
            title = "Teléfono",
            subtitle = userData?.contact?.phoneNumber ?: "Sin teléfono",
            onClick = {
                currentField = ProfileField.PHONE
                showEditDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PosdataClickableCard(
            title = "Contraseña",
            subtitle = "••••••••••••",
            onClick = {
                currentField = ProfileField.PASSWORD
                showEditDialog = true
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- SECCIÓN DE ACCIONES ---

        PosdataPrimaryButton(
            text = "Cerrar Sesión",
            isLoading = isLoading,
            onClick = { viewModel.logout() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PosdataPrimaryButton(
            text = "Eliminar cuenta",
            colorOverride = PosdataRed,
            enabled = !isLoading,
            onClick = { /* Lógica de borrado seguro */ }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    // --- DIÁLOGOS Y OVERLAYS ---

    // Manejo de Errores
    if (uiState is ProfileUiState.Error) {
        val errorMsg = (uiState as ProfileUiState.Error).message
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Error") },
            text = { Text(errorMsg) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("Entendido", color = PosdataBlue)
                }
            }
        )
    }

    // Diálogo de Edición
    if (showEditDialog && userData != null) {
        val initialValue = when (currentField) {
            ProfileField.NAME -> userData.fullName
            ProfileField.EMAIL -> userData.contact.email
            ProfileField.PHONE -> userData.contact.phoneNumber
            else -> ""
        }

        EditInfoDialog(
            title = "Editar ${getFieldLabel(currentField)}",
            initialValue = initialValue,
            isPassword = currentField == ProfileField.PASSWORD,
            onDismiss = { showEditDialog = false },
            onSave = { newValue ->
                // Actualizamos según el campo
                when (currentField) {
                    ProfileField.NAME -> viewModel.updateProfile(newName = newValue)
                    ProfileField.EMAIL -> viewModel.updateProfile(newEmail = newValue)
                    ProfileField.PHONE -> viewModel.updateProfile(newPhone = newValue)
                    else -> {}
                }
                showEditDialog = false
            }
        )
    }
}

// Función auxiliar para etiquetas
private fun getFieldLabel(field: ProfileField): String = when (field) {
    ProfileField.FULL_NAME -> "Nombre"
    ProfileField.EMAIL -> "Email"
    ProfileField.PHONE -> "Teléfono"
    ProfileField.PASSWORD -> "Contraseña"
    else -> ""
}