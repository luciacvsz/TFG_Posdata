package com.posdata.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
fun ProfileScreen(
    userName: String = "Juan Pérez García",
    userEmail: String = "tu@correo.com",
    userPhone: String = "+34 600 000 000"
) {
    Scaffold(
        bottomBar = {
            ProfileBottomBar() // Barra específica para perfil (icono seleccionado)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            // 3. BOTÓN CERRAR SESIÓN (Azul Gradient)
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
                        color = PosdataBlackText // O White si prefieres contraste blanco
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- COMPONENTE REUTILIZABLE: CAJITA DE OPCIÓN ---
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
            .height(80.dp), // Altura fija para uniformidad
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White), // Fondo blanco
        border = BorderStroke(1.dp, PosdataGreyBorder) // Borde gris suave
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Columna Izquierda: Título y Valor
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
                    style = MaterialTheme.typography.bodyMedium, // Texto un poco más pequeño
                    color = PosdataMutedText // Color grisáceo para el valor
                )
            }

            // Icono Derecha: Flechita ">"
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Ir",
                tint = PosdataLightBlue, // Azul clarito para la flecha
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// --- BARRA DE NAVEGACIÓN (Configurada para PERFIL) ---
@Composable
fun ProfileBottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 10.dp,
        modifier = Modifier.height(110.dp)
    ) {
        // ÍTEM 1: INICIO (Desactivado)
        NavigationBarItem(
            selected = false, // <-- FALSO
            onClick = { /* Navegar a Home */ },
            icon = { Icon(Icons.Filled.Home, "Inicio", Modifier.size(42.dp)) },
            label = { Text("Inicio", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )

        // ÍTEM 2: PERFIL (ACTIVADO)
        NavigationBarItem(
            selected = true, // <-- VERDADERO (Icono Azul)
            onClick = { /* Ya estamos aquí */ },
            icon = { Icon(Icons.Filled.Person, "Perfil", Modifier.size(42.dp)) },
            label = { Text("Perfil", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PosdataBlue,
                selectedTextColor = PosdataBlue,
                indicatorColor = PosdataLightBlue.copy(alpha = 0.3f)
            )
        )

        // ÍTEM 3: CONTACTOS
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Groups, "Contactos", Modifier.size(42.dp)) },
            label = { Text("Contactos", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )

        // ÍTEM 4: AJUSTES
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Settings, "Ajustes", Modifier.size(42.dp)) },
            label = { Text("Ajustes", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )
    }
}