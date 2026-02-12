package com.posdata.app.ui.screens.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.posdata.app.R
import com.posdata.app.data.repository.RegisterRepository
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.ui.screens.login.PosdataInput
import com.posdata.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(onBackClick: () -> Unit) {
    // Estados para guardar lo que escribe el usuario
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope ()
    // El 'context' se necesita para mostrar los Toasts de Android
    val context = LocalContext.current
    val userInfo = remember { UserInfo(context) }
    val repository = remember {
        RegisterRepository(
            localApi = RetrofitClient.localInstance,
            cloudApi = RetrofitClient.cloudInstance,
            userInfo = userInfo
        )
    }

    // Estado del scroll (VITAL para pantallas con teclado abierto)
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            onBackClick()
                        },
                        modifier = Modifier.size(70.dp) // Área de toque grande
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = PosdataBlue,
                            modifier = Modifier.size(36.dp) // Flecha visualmente grande
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues) // Respeta la barra superior
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState), // Habilita el scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. LOGO
            Image(
                painter = painterResource(id = R.drawable.logo_posdata),
                contentDescription = "Logo",
                modifier = Modifier.size(110.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. TÍTULO (Alineado a la Izquierda)
            Text(
                text = "Crear Cuenta",
                style = MaterialTheme.typography.titleLarge, // SORA Bold
                color = PosdataBlackText,
                textAlign = TextAlign.Start, // Alineación del texto
                modifier = Modifier
                    .fillMaxWidth() // Ocupa todo el ancho para poder alinearse a la izquierda
                    .padding(bottom = 24.dp)
            )

            // 3. CAMPO NOMBRE
            PosdataInput(
                label = "Nombre",
                placeholder = "Ej: Juan Pérez García",
                value = name,
                onValueChange = { name = it },
                keyboardType = KeyboardType.Text
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. CAMPO EMAIL (Teclado con @)
            PosdataInput(
                label = "Correo Electrónico",
                placeholder = "tu@email.com",
                value = email,
                onValueChange = { email = it },
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. CAMPO TELÉFONO (Teclado Numérico)
            PosdataInput(
                label = "Teléfono",
                placeholder = "+34 600 000 000",
                value = phone,
                onValueChange = { phone = it },
                keyboardType = KeyboardType.Phone
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 6. CAMPO CONTRASEÑA
            PosdataInput(
                label = "Contraseña",
                placeholder = "Ingresa tu contraseña",
                value = password,
                onValueChange = { password = it },
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 7. BOTÓN DE REGISTRO (Con Degradado)
            Button(
                onClick = {

                    isLoading = true

                    scope.launch {
                        val result = repository.performRegistration(name, email, phone, password)

                        result.onSuccess { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }.onFailure { error -> Toast.makeText(context, error.message, Toast.LENGTH_LONG).show() }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(PosdataLightBlue, PosdataBlue)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Crear Cuenta",
                        style = MaterialTheme.typography.labelLarge, // DM Sans Bold
                        color = PosdataBlackText
                    )
                }
            }

            // Espacio extra al final
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- COMPONENTE REUTILIZABLE OPTIMIZADO ---
@Composable
fun PosdataInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            // Usamos labelLarge (Sora Bold 20sp) o titleMedium según tu Type.kt
            style = MaterialTheme.typography.labelLarge,
            color = PosdataBlackText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = PosdataMutedText) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            textStyle = MaterialTheme.typography.bodyLarge, // Texto grande (18sp)
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PosdataGreyBorder,
                focusedBorderColor = PosdataBlue,
                focusedContainerColor = PosdataSurface, // Fondo blanco
                unfocusedContainerColor = PosdataSurface,
                cursorColor = PosdataBlue
            ),
            singleLine = true
        )
    }
}