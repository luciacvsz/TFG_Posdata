package com.posdata.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
fun SettingsContent() {
    var isLargeTextEnabled by remember { mutableStateOf(false) } // Apagado
    var isSoundEnabled by remember { mutableStateOf(false) }     // Apagado
    var isExplanationsEnabled by remember { mutableStateOf(true) } // Encendido (Azul)
    var isExhaustiveModeEnabled by remember { mutableStateOf(true) } // Encendido (Azul)

    Column(
        modifier = Modifier
            .fillMaxSize()
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

fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)