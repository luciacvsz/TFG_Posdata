package com.posdata.app.ui.screens.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.posdata.app.R
import com.posdata.app.ui.components.PosdataInput
import com.posdata.app.ui.components.PosdataPrimaryButton
import com.posdata.app.ui.components.PosdataSimpleDialog

/**
 * Registration screen of the application.
 *
 * Displays a form with fields for full name, phone number, email, and password.
 * Shows an error dialog if the registration fails, and triggers [onRegisterSuccess]
 * when it completes successfully. Navigation on success is driven by the reactive
 * [com.posdata.app.data.local.UserDataStore.userData] flow in [com.posdata.app.MainActivity].
 *
 * @param viewModel ViewModel managing the registration state and business logic.
 * @param onBackClick Callback invoked when the user taps the back arrow.
 * @param onRegisterSuccess Callback invoked when the registration completes successfully.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onRegisterSuccess()
        }
    }

    state.errorMessage?.let {
        PosdataSimpleDialog(
            title = "Error de registro",
            message = it,
            onDismiss = { viewModel.dismissError() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBackClick()
                        },
                        modifier = Modifier.size(70.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.logo_posdata),
                contentDescription = "Logo",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            PosdataInput(
                label = "Nombre",
                placeholder = "Ej: Juan Pérez García",
                value = fullName,
                onValueChange = { fullName = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            PosdataInput(
                label = "Teléfono",
                placeholder = "+34 600 000 000",
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            PosdataInput(
                label = "Correo Electrónico",
                placeholder = "tu@email.com",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            PosdataInput(
                label = "Contraseña",
                placeholder = "Ingresa tu contraseña",
                value = password,
                onValueChange = { password = it },
                isPassword = true,
                keyboardType = KeyboardType.Password

            )

            Spacer(modifier = Modifier.height(32.dp))

            PosdataPrimaryButton(
                text = "Crear Cuenta",
                onClick = {
                    viewModel.register(fullName, phoneNumber, email, password) },
                isLoading = state.isRegistering
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}