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
import androidx.compose.ui.unit.sp
import com.posdata.app.network.LoginRequest
import com.posdata.app.network.RetrofitClient
import com.posdata.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope ()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(35.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        //LOGO
        Image(
            painter = painterResource(id = R.drawable.logo_posdata),
            contentDescription = "Logo Posdata",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Fit
        )

        //SYSTEM NAME
        Image(
            painter = painterResource(id = R.drawable.nombre_posdata),
            contentDescription = "Nombre Posdata",
            modifier = Modifier
                .width(200.dp)
                .height(110.dp),
            contentScale = ContentScale.Fit
        )

        //EMAIL
        PosdataInput(
            label = "Correo Electrónico",
            placeholder = "tu@email.com",
            value = email,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // PASSWORD
        PosdataInput(
            label = "Contraseña",
            placeholder = "Ingresa tu contraseña",
            value = password,
            onValueChange = { password = it },
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        //LOGIN
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()){
                    Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                isLoading = true

                scope.launch {
                    try {
                        val request = LoginRequest(email, password)
                        val response = RetrofitClient.instance.login(request)

                        isLoading = false

                        if (response.isSuccessful && response.body()?.success == true) {
                            val data = response.body()!!
                            Toast.makeText(context, "¡Bienvenido!", Toast.LENGTH_LONG).show()
                        } else {
                            val errorMsg = response.body()?.message ?: "Error desconocido"
                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        Toast.makeText(context, "No se puede conectar con el servidor.", Toast.LENGTH_LONG).show()
                        e.printStackTrace()
                    }
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

        // FORGOTTEN PASSWORD
        TextButton(onClick = { /* Recuperar */ }) {
            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodyLarge,
                color = PosdataBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DIVIDE
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

        // REGISTER
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