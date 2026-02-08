package com.posdata.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posdata.app.ui.theme.*
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun SettingsScreen() {
    // ESTADOS DE LOS INTERRUPTORES (Para que se muevan al tocar)
    // En una app real, esto vendría de una base de datos o preferencias
    var isLargeTextEnabled by remember { mutableStateOf(false) } // Apagado
    var isSoundEnabled by remember { mutableStateOf(false) }     // Apagado
    var isExplanationsEnabled by remember { mutableStateOf(true) } // Encendido (Azul)
    var isExhaustiveModeEnabled by remember { mutableStateOf(true) } // Encendido (Azul)

    Scaffold(
        bottomBar = {
            SettingsBottomBar() // Barra con "Ajustes" seleccionado
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 36.dp) // Margen lateral estándar
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // 1. TÍTULO
            Text(
                text = "Ajustes",
                style = MaterialTheme.typography.headlineLarge, // Sora Bold
                color = PosdataBlackText,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 2. LISTA DE AJUSTES (Usando nuestro componente personalizado)

            // Opción: Texto Grande
            SettingsSwitch(
                label = "Texto\nGrande", // \n para salto de línea
                isChecked = isLargeTextEnabled,
                onCheckedChange = { isLargeTextEnabled = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Opción: Sonido
            SettingsSwitch(
                label = "Sonido de\nNotificación",
                isChecked = isSoundEnabled,
                onCheckedChange = { isSoundEnabled = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Opción: Explicaciones
            SettingsSwitch(
                label = "Explicaciones",
                isChecked = isExplanationsEnabled,
                onCheckedChange = { isExplanationsEnabled = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Opción: Modo Exhaustivo
            SettingsSwitch(
                label = "Modo\nExhaustivo",
                isChecked = isExhaustiveModeEnabled,
                onCheckedChange = { isExhaustiveModeEnabled = it }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- COMPONENTE REUTILIZABLE: INTERRUPTOR PERSONALIZADO ---
@Composable
fun SettingsSwitch(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // Texto a izq, Switch a der
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Texto de la opción
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium, // DM Sans Bold 20sp (ajustado en Type.kt)
            fontSize = 20.sp, // Aseguramos tamaño grande
            fontWeight = FontWeight.Bold,
            color = PosdataBlackText,
            lineHeight = 26.sp // Para que no se peguen las líneas si hay salto
        )

        // El Interruptor (Switch)
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                // ESTADO ENCENDIDO (ON)
                checkedThumbColor = Color.White,
                checkedTrackColor = PosdataLightBlue, // Tu azul clarito (o PosdataBlue)
                checkedBorderColor = Color.Transparent,

                // ESTADO APAGADO (OFF)
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = PosdataMutedText, // Gris (relleno, no borde)
                uncheckedBorderColor = Color.Transparent // Quitamos el borde para que parezca relleno
            ),
            // Hacemos el switch un poco más grande visualmente si es necesario
            modifier = Modifier.scale(1.1f)
        )
    }
}

// Pequeña utilidad para escalar el switch si lo ves pequeño
fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)
// --- BARRA DE NAVEGACIÓN (Ajustes Seleccionado) ---
@Composable
fun SettingsBottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 10.dp,
        modifier = Modifier.height(110.dp)
    ) {
        // INICIO
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Home, "Inicio", Modifier.size(42.dp)) },
            label = { Text("Inicio", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )
        // PERFIL
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Person, "Perfil", Modifier.size(42.dp)) },
            label = { Text("Perfil", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )
        // CONTACTOS
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Groups, "Contactos", Modifier.size(42.dp)) },
            label = { Text("Contactos", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )
        // AJUSTES (ACTIVO)
        NavigationBarItem(
            selected = true, // <--- ESTE ES EL ACTIVO
            onClick = { /* Estamos aquí */ },
            icon = { Icon(Icons.Filled.Settings, "Ajustes", Modifier.size(42.dp)) },
            label = { Text("Ajustes", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PosdataBlue,
                selectedTextColor = PosdataBlue,
                indicatorColor = PosdataLightBlue.copy(alpha = 0.3f)
            )
        )
    }
}