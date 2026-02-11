package com.posdata.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posdata.app.data.UserInfo
import com.posdata.app.model.*
import com.posdata.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsContent(
    userData: UserData?
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userInfo = remember { UserInfo(context) }

    // Obtenemos las preferencias actuales
    val prefs = userData?.preferences ?: AppPreferences()

    // --- HELPER PARA GUARDAR ---
    fun saveSettings(
        fontSize: AppFontSize = prefs.fontSize,
        sound: AppNotificationSound = prefs.notificationSound,
        color: AppColorScheme = prefs.colorScheme,
        exhaustivity: AppExhaustivity = prefs.exhaustivity,
        explanation: AppExplanationMode = prefs.explanationMode
    ) {
        scope.launch {
            userInfo.updateSettings(
                fontSize = fontSize,
                sound = sound,
                color = color,
                exhaustivity = exhaustivity,
                explanation = explanation
            )
        }
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
            text = "Ajustes",
            style = MaterialTheme.typography.headlineLarge,
            color = PosdataBlackText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))

        // --- SECCIÓN: APARIENCIA (NUEVA GRID DE 6 COLORES) ---
        Text(
            text = "Esquema de Color",
            style = MaterialTheme.typography.titleSmall,
            color = PosdataMutedText,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        // FILA 1: Estándar, Alto Contraste, Protanopia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ColorOptionItem(
                color = PosdataBlue, // Azul Estándar
                label = "Estándar",
                isSelected = prefs.colorScheme == AppColorScheme.STANDARD,
                onClick = { saveSettings(color = AppColorScheme.STANDARD) }
            )
            ColorOptionItem(
                color = Color.Black, // Negro
                label = "Contraste",
                isSelected = prefs.colorScheme == AppColorScheme.HIGH_CONTRAST,
                onClick = { saveSettings(color = AppColorScheme.HIGH_CONTRAST) }
            )
            ColorOptionItem(
                color = Color(0xFF8E44AD), // Violeta (distinguible para Protanopia)
                label = "Protanopia",
                isSelected = prefs.colorScheme == AppColorScheme.PROTANOPIA,
                onClick = { saveSettings(color = AppColorScheme.PROTANOPIA) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // FILA 2: Deuteranopia, Tritanopia, Acromatopsia
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ColorOptionItem(
                color = Color(0xFFD35400), // Naranja/Ocre (distinguible Deuteranopia)
                label = "Deutera...", // Abreviado para que quepa visualmente
                fullName = "Deuteranopia",
                isSelected = prefs.colorScheme == AppColorScheme.DEUTERANOPIA,
                onClick = { saveSettings(color = AppColorScheme.DEUTERANOPIA) }
            )
            ColorOptionItem(
                color = Color(0xFF16A085), // Verde Azulado/Teal (distinguible Tritanopia)
                label = "Tritanopia",
                isSelected = prefs.colorScheme == AppColorScheme.TRITANOPIA,
                onClick = { saveSettings(color = AppColorScheme.TRITANOPIA) }
            )
            ColorOptionItem(
                color = Color.Gray, // Gris
                label = "Acroma...",
                fullName = "Acromatopsia",
                isSelected = prefs.colorScheme == AppColorScheme.ACHROMATOPSIA,
                onClick = { saveSettings(color = AppColorScheme.ACHROMATOPSIA) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(color = PosdataGreyBorder)
        Spacer(modifier = Modifier.height(32.dp))

        // --- SECCIÓN: RESTO DE PREFERENCIAS ---

        // 1. Texto Grande
        SettingsSwitch(
            label = "Texto\nGrande",
            isChecked = prefs.fontSize == AppFontSize.LARGE,
            onCheckedChange = { isChecked ->
                saveSettings(fontSize = if (isChecked) AppFontSize.LARGE else AppFontSize.REGULAR)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Sonido
        SettingsSwitch(
            label = "Sonido de\nNotificación",
            isChecked = prefs.notificationSound == AppNotificationSound.ON,
            onCheckedChange = { isChecked ->
                saveSettings(sound = if (isChecked) AppNotificationSound.ON else AppNotificationSound.OFF)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Explicaciones
        SettingsSwitch(
            label = "Explicaciones\ndetalladas",
            isChecked = prefs.explanationMode == AppExplanationMode.ON,
            onCheckedChange = { isChecked ->
                saveSettings(explanation = if (isChecked) AppExplanationMode.ON else AppExplanationMode.OFF)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Modo Exhaustivo
        SettingsSwitch(
            label = "Análisis\nExhaustivo",
            isChecked = prefs.exhaustivity == AppExhaustivity.ENHANCED,
            onCheckedChange = { isChecked ->
                saveSettings(exhaustivity = if (isChecked) AppExhaustivity.ENHANCED else AppExhaustivity.REGULAR)
            }
        )

        Spacer(modifier = Modifier.height(50.dp))
    }
}

// --- COMPONENTE: OPCIÓN DE COLOR ---
@Composable
fun RowScope.ColorOptionItem( // RowScope permite usar Weight si fuera necesario
    color: Color,
    label: String,
    fullName: String = label,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .weight(1f) // Esto asegura que las 3 opciones se repartan el ancho igual
    ) {
        Box(
            modifier = Modifier
                .size(50.dp) // Un poco más pequeñas para que quepan bien
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (isSelected) 3.dp else 0.dp,
                    color = if (isSelected) PosdataBlue else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Seleccionado",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall, // Texto más pequeño
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PosdataBlackText else PosdataMutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- COMPONENTE: INTERRUPTOR ---
@Composable
fun SettingsSwitch(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = PosdataBlackText,
            lineHeight = 26.sp
        )

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PosdataLightBlue,
                checkedBorderColor = Color.Transparent,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = PosdataMutedText,
                uncheckedBorderColor = Color.Transparent
            ),
            modifier = Modifier.scale(1.1f)
        )
    }
}