package com.posdata.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posdata.app.model.UserData
import com.posdata.app.ui.components.PosdataClickableCard
import com.posdata.app.ui.components.PosdataEditDialog
import com.posdata.app.ui.components.PosdataPrimaryButton
import com.posdata.app.ui.components.PosdataStatusDialog
import com.posdata.app.ui.theme.*

@Composable
fun ProfileContent(
    userData: UserData?,
    viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showEditDialog by remember { mutableStateOf(false) }
    var currentField by remember { mutableStateOf(ProfileField.NONE) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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

        PosdataClickableCard(
            title = "Nombre",
            subtitle = userData?.fullName ?: "Cargando...",
            onClick = {
                currentField = ProfileField.FULL_NAME
                showEditDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PosdataClickableCard(
            title = "Teléfono",
            subtitle = userData?.contact?.phoneNumber ?: "Cargando...",
            onClick = {
                currentField = ProfileField.PHONE_NUMBER
                showEditDialog = true
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PosdataClickableCard(
            title = "Correo Electrónico",
            subtitle = userData?.contact?.email ?: "Cargando...",
            onClick = {
                currentField = ProfileField.EMAIL
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
            onClick = { showDeleteConfirm = true }
        )

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (uiState is ProfileUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PosdataBlue)
        }
    }

    when (val state = uiState) {
        is ProfileUiState.Success -> {
            PosdataStatusDialog(
                isSuccess = true,
                message = state.message,
                onDismiss = { viewModel.resetState() }
            )
        }
        is ProfileUiState.Error -> {
            PosdataStatusDialog(
                isSuccess = false,
                message = state.message,
                onDismiss = { viewModel.resetState() }
            )
        }
        else -> {}
    }

    if (showEditDialog && userData != null) {
        val initialValue = when (currentField) {
            ProfileField.FULL_NAME -> userData.fullName
            ProfileField.PHONE_NUMBER -> userData.contact.phoneNumber
            ProfileField.EMAIL -> userData.contact.email
            ProfileField.PASSWORD -> {}
            else -> ""
        }

        PosdataEditDialog(
            title = "Editar ${getFieldLabel(currentField)}",
            label = getFieldLabel(currentField),
            initialValue = initialValue as String,
            isPassword = currentField == ProfileField.PASSWORD,
            onDismiss = { showEditDialog = false },
            onSave = { newValue ->
                when (currentField) {
                    ProfileField.FULL_NAME -> viewModel.updateProfileField(field = ProfileField.FULL_NAME, newValue = newValue)
                    ProfileField.PHONE_NUMBER -> viewModel.updateProfileField(field = ProfileField.PHONE_NUMBER, newValue = newValue)
                    ProfileField.EMAIL -> viewModel.updateProfileField(field = ProfileField.EMAIL, newValue = newValue)
                    else -> {}
                }
                showEditDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar cuenta?", fontWeight = FontWeight.Bold) },
            text = { Text("Esta acción borrará tus datos permanentemente de la nube y del dispositivo. No se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAccount()
                    }
                ) {
                    Text("ELIMINAR", color = PosdataRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("CANCELAR", color = PosdataMutedText)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

private fun getFieldLabel(field: ProfileField): String = when (field) {
    ProfileField.FULL_NAME -> "Nombre"
    ProfileField.PHONE_NUMBER -> "Teléfono"
    ProfileField.EMAIL -> "Email"
    ProfileField.PASSWORD -> "Contraseña"
    else -> ""
}