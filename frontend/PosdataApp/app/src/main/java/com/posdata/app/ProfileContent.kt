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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posdata.app.ui.theme.*

@Composable
fun ProfileContent(
    userName: String = "Juan Pérez García",
    userEmail: String = "tu@correo.com",
    userPhone: String = "+34 600 000 000"
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(32.dp))

        // 1. TÍTULO "Mi Perfil"
        Text(
            text = "Mi Perfil",
            style = MaterialTheme.typography.headlineLarge, // Sora Bold
            color = PosdataBlackText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. LISTA DE OPCIONES (Usando nuestro componente ProfileOption)

        // Nombre
        ProfileOption(
            label = "Nombre",
            value = userName,
            onClick = { /* Editar nombre */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Correo
        ProfileOption(
            label = "Correo Electrónico",
            value = userEmail,
            onClick = { /* Editar correo */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Teléfono
        ProfileOption(
            label = "Teléfono",
            value = userPhone,
            onClick = { /* Editar teléfono */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contraseña (Value hardcodeado con puntitos)
        ProfileOption(
            label = "Contraseña",
            value = "................",
            onClick = { /* Cambiar contraseña */ }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { /* Lógica Logout */ },
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
}

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