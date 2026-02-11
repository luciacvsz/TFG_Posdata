package com.posdata.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posdata.app.data.UserInfo
import com.posdata.app.model.TrustedContact
import com.posdata.app.model.UserData
import com.posdata.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ContactsContent(
    userData: UserData?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userInfo = remember { UserInfo(context) }

    // ESTADOS PARA LOS DIÁLOGOS
    var showAddDialog by remember { mutableStateOf(false) }
    var contactToEdit by remember { mutableStateOf<TrustedContact?>(null) }
    var indexToEdit by remember { mutableStateOf(-1) }

    // LISTA DE CONTACTOS REALES (Si es null, usamos lista vacía)
    val myContacts = userData?.trustedContacts ?: emptyList()

    // FUNCIÓN PARA GUARDAR LA NUEVA LISTA EN DISCO
    fun saveNewList(newList: List<TrustedContact>) {
        if (userData == null) return

        scope.launch {
            userInfo.saveUserSession(
                userId = userData.userId,
                sessionToken = userData.sessionToken,
                tokens = userData.tokens,
                fullName = userData.fullName,
                contact = userData.contact,
                preferences = userData.preferences,
                trustedContacts = newList // <--- AQUÍ GUARDAMOS LA LISTA MODIFICADA
            )
        }
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
                    color = PosdataBlackText,
                    textAlign = TextAlign.Start,
                    lineHeight = 60.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- LÓGICA DE LISTA VACÍA ---
                if (myContacts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes contactos de confianza añadidos.\n¡Añade uno para estar más seguro!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = PosdataMutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // --- LISTA DE CONTACTOS ---
                    myContacts.forEachIndexed { index, contact ->
                        ContactCard(
                            name = contact.name,
                            relation = contact.role, // Asumo que tu modelo tiene 'role' o 'relation'
                            phone = contact.phoneNumber,
                            email = contact.email,
                            onClick = {
                                // ABRIR MODO EDICIÓN
                                indexToEdit = index
                                contactToEdit = contact
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // BOTÓN AÑADIR (Pegado abajo)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp, bottom = 24.dp)
            ) {
                AddContactButton(onClick = { showAddDialog = true })
            }
        }

        // --- DIÁLOGO DE AÑADIR ---
        if (showAddDialog) {
            ContactDialog(
                title = "Añadir Contacto",
                initialName = "",
                initialRole = "",
                initialPhone = "",
                initialEmail = "",
                onDismiss = { showAddDialog = false },
                onSave = { name, role, phone, email ->
                    // CREAMOS EL NUEVO CONTACTO
                    val newContact = TrustedContact(
                        name = name,
                        role = role,
                        phoneNumber = phone.ifBlank { null }, // Si está vacío, guardamos null
                        email = email.ifBlank { null }
                    )
                    // GUARDAMOS: Lista actual + Nuevo
                    saveNewList(myContacts + newContact)
                    showAddDialog = false
                }
            )
        }

        // --- DIÁLOGO DE EDITAR / BORRAR ---
        if (contactToEdit != null && indexToEdit != -1) {
            ContactDialog(
                title = "Editar Contacto",
                initialName = contactToEdit!!.name,
                initialRole = contactToEdit!!.role,
                initialPhone = contactToEdit!!.phoneNumber ?: "",
                initialEmail = contactToEdit!!.email ?: "",
                isEditMode = true, // Activa el botón de borrar
                onDismiss = {
                    contactToEdit = null
                    indexToEdit = -1
                },
                onSave = { name, role, phone, email ->
                    // CREAMOS EL CONTACTO MODIFICADO
                    val updatedContact = TrustedContact(
                        name = name,
                        role = role,
                        phoneNumber = phone.ifBlank { null },
                        email = email.ifBlank { null }
                    )
                    // ACTUALIZAMOS LA LISTA
                    val newList = myContacts.toMutableList()
                    newList[indexToEdit] = updatedContact

                    saveNewList(newList)

                    contactToEdit = null
                    indexToEdit = -1
                },
                onDelete = {
                    // BORRAMOS EL CONTACTO
                    val newList = myContacts.toMutableList()
                    newList.removeAt(indexToEdit)

                    saveNewList(newList)

                    contactToEdit = null
                    indexToEdit = -1
                }
            )
        }
    }
}

// --- COMPONENTE: DIÁLOGO DE CONTACTO (Súper potente) ---
@Composable
fun ContactDialog(
    title: String,
    initialName: String,
    initialRole: String,
    initialPhone: String,
    initialEmail: String,
    isEditMode: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    // Estados internos del formulario
    var name by remember { mutableStateOf(initialName) }
    var role by remember { mutableStateOf(initialRole) }
    var phone by remember { mutableStateOf(initialPhone) }
    var email by remember { mutableStateOf(initialEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()) // Por si el teclado tapa campos
            ) {
                // CAMPO NOMBRE (OBLIGATORIO)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre (Ej: Mamá)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // CAMPO RELACIÓN (OBLIGATORIO)
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Relación (Ej: Madre, Amigo)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // CAMPO TELÉFONO (OPCIONAL)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Teléfono (Opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // CAMPO EMAIL (OPCIONAL)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Opcional)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Validación simple: Nombre y Rol obligatorios
                    if (name.isNotBlank() && role.isNotBlank()) {
                        onSave(name, role, phone, email)
                    }
                }
            ) {
                Text("Guardar", color = PosdataBlue, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (isEditMode && onDelete != null) {
                    // BOTÓN BORRAR (ROJO)
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Borrar", tint = PosdataRed)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = PosdataMutedText)
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

// ... TUS COMPONENTES ORIGINALES (AddContactButton y ContactCard) ...
// (Pégalos aquí tal cual los tenías, no hace falta cambiarlos)

@Composable
fun AddContactButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .drawBehind {
                drawRoundRect(
                    color = PosdataLightBlue,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                    ),
                    cornerRadius = CornerRadius(20.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "+ Añadir Contacto",
                style = MaterialTheme.typography.labelLarge,
                color = PosdataLightBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ContactCard(
    name: String,
    relation: String,
    phone: String? = null,
    email: String? = null,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, PosdataGreyBorder)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                            append("$name ")
                        }
                        append("- $relation")
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = PosdataBlackText,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (phone != null) {
                    Text(text = phone, style = MaterialTheme.typography.bodyMedium, color = PosdataMutedText)
                }
                if (email != null) {
                    Text(text = email, style = MaterialTheme.typography.bodyMedium, color = PosdataMutedText)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ver",
                tint = PosdataLightBlue,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}