package com.posdata.app

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.posdata.app.data.LoginRepository
import com.posdata.app.data.UserInfo
import com.posdata.app.network.RetrofitClient
import com.posdata.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    // 1. ESTADO DE LA UI
    // 'remember' guarda el valor aunque la pantalla se redibuje
    // 'mutableStateOf' avisa a Compose para redibujar si el valor cambia
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // 2. HERRAMIENTAS Y CONTEXTO
    // Necesitamos un 'scope' para lanzar tareas en segundo plano (Corrutinas) al pulsar botones
    val scope = rememberCoroutineScope ()
    // El 'context' se necesita para mostrar los Toasts de Android
    val context = LocalContext.current
    val userInfo = remember { UserInfo(context) }
    val repository = remember {
        LoginRepository(
            localApi = RetrofitClient.localInstance,
            cloudApi = RetrofitClient.cloudInstance,
            userInfo = userInfo
        )
    }

    // 3. DISEÑO DE LA PANTALLA
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda la pantalla
            .background(MaterialTheme.colorScheme.background) // Color de fondo del tema
            .padding(35.dp), // Margen interno general
        horizontalAlignment = Alignment.CenterHorizontally, // Centrar elementos horizontalmente
        verticalArrangement = Arrangement.Center // Centrar elementos verticalmente
    ) {

        // --- SECCIÓN: CABECERA ---

        Image(
            painter = painterResource(id = R.drawable.logo_posdata),
            contentDescription = "Logo Posdata",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )

        Image(
            painter = painterResource(id = R.drawable.nombre_posdata),
            contentDescription = "Nombre Posdata",
            modifier = Modifier
                .width(200.dp)
                .height(110.dp),
            contentScale = ContentScale.Fit
        )

        // --- SECCIÓN: FORMULARIO ---

        // Input para el Email
        PosdataInput(
            label = "Correo Electrónico",
            placeholder = "tu@email.com",
            value = email,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Input para la Contraseña
        PosdataInput(
            label = "Contraseña",
            placeholder = "Ingresa tu contraseña",
            value = password,
            onValueChange = { password = it },
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECCIÓN: BOTÓN DE LOGIN ---
        Button(
            onClick = {
                // Primero, comprobamos que ningún campo esté vacío
                if (email.isBlank() || password.isBlank()){
                    Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // Iniciamos la carga
                isLoading = true

                // Hacemos la petición de red. Usamos 'scope.launch' porque Retrofit es una función 'suspend'
                // y no se puede llamar directamente en el hilo principal de la UI
                scope.launch {
                    val result = repository.performLoginAndSync(email, password)

                    result.onSuccess { message -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }.onFailure { error -> Toast.makeText(context, error.message, Toast.LENGTH_LONG).show() }
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            enabled = !isLoading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isLoading) SolidColor(PosdataGreyBorder) else
                        Brush.horizontalGradient(colors = listOf(PosdataLightBlue, PosdataBlue))
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = PosdataWhite, modifier = Modifier.size(30.dp))
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        style = MaterialTheme.typography.labelLarge,
                        color = PosdataBlackText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECCIÓN: OLVIDO DE CONTRASEÑA ---
        TextButton(onClick = { /* Recuperar */ }) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodyLarge,
                color = PosdataBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN: SEPARADOR 'O' ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = PosdataGreyBorder)
            Text(
                text = " o ",
                color = PosdataMutedText,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = PosdataGreyBorder)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SECCIÓN: BOTÓN DE REGISTRO ---
        OutlinedButton(
            onClick = { /* Registro */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, PosdataGreyBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = PosdataSurface,
                contentColor = PosdataBlackText)
        ) {
            Text(
                text = "Crear Cuenta Nueva",
                style = MaterialTheme.typography.labelLarge // DM Sans Bold
            )
        }
    }
}

// 4. COMPONENTE REUTILIZABLE: INPUT DE TEXTO
@Composable
fun PosdataInput(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
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
            textStyle = MaterialTheme.typography.bodyLarge, // DM Sans 18sp (Grande para leer bien)
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = PosdataGreyBorder,
                focusedBorderColor = PosdataBlue,
                focusedContainerColor = PosdataSurface,
                unfocusedContainerColor = PosdataSurface,
                cursorColor = PosdataBlue
            ),
            singleLine = true
        )
    }
}