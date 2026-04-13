package com.posdata.app.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.posdata.app.R
import com.posdata.app.ui.components.PosdataInput
import com.posdata.app.ui.components.PosdataPrimaryButton
import com.posdata.app.ui.components.PosdataSimpleDialog

/**
 * Login screen of the application.
 *
 * Displays the app logo, email and password fields, and buttons to log in
 * or navigate to the registration screen. Shows an error dialog if the
 * login fails, and triggers [onLoginSuccess] when it succeeds.
 *
 * @param viewModel ViewModel managing the login state and business logic.
 * @param onRegisterClick Callback invoked when the user taps "Crear Cuenta Nueva".
 * @param onLoginSuccess Callback invoked when the login completes successfully.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onLoginSuccess()
        }
    }

    state.errorMessage?.let {
        PosdataSimpleDialog(
            title = "Error de acceso",
            message = it,
            onDismiss = { viewModel.dismissError() }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(35.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_posdata),
            contentDescription = "Logo Posdata",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )

        Image(
            painter = painterResource(id = R.drawable.nombre_posdata),
            contentDescription = "Nombre Posdata",
            modifier = Modifier.width(200.dp).height(110.dp),
            contentScale = ContentScale.Fit
        )

        PosdataInput(
            label = "Correo Electrónico",
            placeholder = "tu@email.com",
            value = email,
            onValueChange = { email = it },
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(20.dp))

        PosdataInput(
            label = "Contraseña",
            placeholder = "Ingresa tu contraseña",
            value = password,
            onValueChange = { password = it },
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        PosdataPrimaryButton(
            text = "Iniciar Sesión",
            onClick = {
                viewModel.login(email, password) },
            isLoading = state.isLogging
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = " o ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outline
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        PosdataPrimaryButton(
            text = "Crear Cuenta Nueva",
            onClick = onRegisterClick,
        )
    }
}