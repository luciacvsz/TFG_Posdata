package com.posdata.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posdata.app.data.UserInfo
import com.posdata.app.model.UserData
import com.posdata.app.ui.theme.*
import kotlinx.coroutines.launch

// Enumerado para saber qué estamos editando
enum class ProfileField { NONE, NAME, EMAIL, PHONE, PASSWORD }

@Composable
fun ProfileContent(
    userData: UserData? // Ahora recibimos el objeto completo
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userInfo = remember { UserInfo(context) }

    // ESTADOS PARA EL DIÁLOGO
    var showDialog by remember { mutableStateOf(false) }
    var currentField by remember { mutableStateOf(ProfileField.NONE) }

    // Función auxiliar para abrir el diálogo
    fun openEdit(field: ProfileField) {
        currentField = field
        showDialog = true
    }

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

        // 1. NOMBRE
        ProfileOption(
            label = "Nombre",
            value = userData?.fullName ?: "Cargando...",
            onClick = { openEdit(ProfileField.NAME) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. CORREO
        ProfileOption(
            label = "Correo Electrónico",
            value = userData?.contact?.email ?: "Sin email",
            onClick = { openEdit(ProfileField.EMAIL) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3. TELÉFONO
        ProfileOption(
            label = "Teléfono",
            value = userData?.contact?.phoneNumber ?: "Sin teléfono",
            onClick = { openEdit(ProfileField.PHONE) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. CONTRASEÑA
        ProfileOption(
            label = "Contraseña",
            value = "••••••••••••", // Ocultamos la contraseña real
            onClick = { openEdit(ProfileField.PASSWORD) }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // BOTÓN LOGOUT (CON LÓGICA REAL)
        Button(
            onClick = {
                scope.launch {
                    //userInfo.logout()
                    // MainActivity detectará el cambio y mostrará LoginScreen automáticamente
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
                    text = "Cerrar Sesión",
                    style = MaterialTheme.typography.labelLarge,
                    fontSize = 18.sp,
                    color = PosdataBlackText
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // --- EL DIÁLOGO DE EDICIÓN ---
    if (showDialog && userData != null) {
        val initialValue = when (currentField) {
            ProfileField.NAME -> userData.fullName
            ProfileField.EMAIL -> userData.contact.email
            ProfileField.PHONE -> userData.contact.phoneNumber
            ProfileField.PASSWORD -> "" // La contraseña empieza vacía
            else -> ""
        }

        EditInfoDialog(
            title = "Editar ${getFieldLabel(currentField)}",
            initialValue = initialValue,
            isPassword = currentField == ProfileField.PASSWORD,
            onDismiss = { showDialog = false },
            onSave = { newValue ->
                scope.launch {
                    // AQUÍ ACTUALIZAMOS LOS DATOS LOCALES
                    // (Nota: En una app real, aquí llamarías también a la API para guardar en la nube)

                    val currentContact = userData.contact

                    when (currentField) {
                        ProfileField.NAME -> userInfo.saveUserSession(
                            userId = userData.userId,
                            sessionToken = userData.sessionToken,
                            tokens = userData.tokens,
                            fullName = newValue, // <--- Actualizamos Nombre
                            contact = currentContact,
                            preferences = userData.preferences,
                            trustedContacts = userData.trustedContacts
                        )
                        ProfileField.EMAIL -> userInfo.saveUserSession(
                            userId = userData.userId,
                            sessionToken = userData.sessionToken,
                            tokens = userData.tokens,
                            fullName = userData.fullName,
                            contact = currentContact.copy(email = newValue), // <--- Actualizamos Email
                            preferences = userData.preferences,
                            trustedContacts = userData.trustedContacts
                        )
                        ProfileField.PHONE -> userInfo.saveUserSession(
                            userId = userData.userId,
                            sessionToken = userData.sessionToken,
                            tokens = userData.tokens,
                            fullName = userData.fullName,
                            contact = currentContact.copy(phoneNumber = newValue), // <--- Actualizamos Teléfono
                            preferences = userData.preferences,
                            trustedContacts = userData.trustedContacts
                        )
                        ProfileField.PASSWORD -> {
                            // Para la contraseña, normalmente solo se guarda en el token de sesión o se llama a la API
                            // No solemos guardarla en plano en UserInfo, así que aquí iría la llamada a la API
                        }
                        else -> {}
                    }
                    showDialog = false
                }
            }
        )
    }
}

// --- UTILIDADES ---

fun getFieldLabel(field: ProfileField): String {
    return when (field) {
        ProfileField.NAME -> "Nombre"
        ProfileField.EMAIL -> "Email"
        ProfileField.PHONE -> "Teléfono"
        ProfileField.PASSWORD -> "Contraseña"
        else -> ""
    }
}

@Composable
fun EditInfoDialog(
    title: String,
    initialValue: String,
    isPassword: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Nuevo valor") },
                singleLine = true,
                visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(text) }) {
                Text("Guardar", color = PosdataBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = PosdataMutedText)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// Mantenemos tu componente ProfileOption original
@Composable
fun ProfileOption(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PosdataGreyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = PosdataBlackText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PosdataMutedText
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ir",
                tint = PosdataLightBlue,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}