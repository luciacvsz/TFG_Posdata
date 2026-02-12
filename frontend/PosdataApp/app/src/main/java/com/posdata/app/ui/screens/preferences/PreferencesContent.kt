package com.posdata.app.ui.screens.preferences

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posdata.app.model.*
import com.posdata.app.ui.theme.*

@Composable
fun PreferencesContent(
    userData: UserData?,
    viewModel: PreferencesViewModel = viewModel(
        factory = SettingsViewModelFactory(LocalContext.current)
    )
) {

    val prefs = userData?.preferences ?: AppPreferences()

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

        Text(
            text = "Esquema de Color",
            style = MaterialTheme.typography.titleSmall,
            color = PosdataMutedText,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ColorOptionItem(
                color = PosdataBlue, // Azul Estándar
                label = "Estándar",
                isSelected = prefs.colorScheme == AppColorScheme.STANDARD,
                onClick = { viewModel.updateColorScheme(AppColorScheme.STANDARD) }
            )
            ColorOptionItem(
                color = Color.Black, // Negro
                label = "Contraste",
                isSelected = prefs.colorScheme == AppColorScheme.HIGH_CONTRAST,
                onClick = { viewModel.updateColorScheme(AppColorScheme.HIGH_CONTRAST) }
            )
            ColorOptionItem(
                color = Color(0xFF8E44AD), // Violeta (distinguible para Protanopia)
                label = "Protanopia",
                isSelected = prefs.colorScheme == AppColorScheme.PROTANOPIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.PROTANOPIA) }
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
                isSelected = prefs.colorScheme == AppColorScheme.DEUTERANOPIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.DEUTERANOPIA) }
            )
            ColorOptionItem(
                color = Color(0xFF16A085), // Verde Azulado/Teal (distinguible Tritanopia)
                label = "Tritanopia",
                isSelected = prefs.colorScheme == AppColorScheme.TRITANOPIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.TRITANOPIA) }
            )
            ColorOptionItem(
                color = Color.Gray, // Gris
                label = "Acroma...",
                isSelected = prefs.colorScheme == AppColorScheme.ACHROMATOPSIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.ACHROMATOPSIA) }
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
               viewModel.updateFontSize(isChecked)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Sonido
        SettingsSwitch(
            label = "Sonido de\nNotificación",
            isChecked = prefs.notificationSound == AppNotificationSound.ON,
            onCheckedChange = { isChecked ->
                viewModel.updateNotificationSound(isChecked)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Explicaciones
        SettingsSwitch(
            label = "Explicaciones\ndetalladas",
            isChecked = prefs.explanationMode == AppExplanationMode.ON,
            onCheckedChange = { isChecked ->
                viewModel.updateExplanationMode(isChecked)            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 4. Modo Exhaustivo
        SettingsSwitch(
            label = "Análisis\nExhaustivo",
            isChecked = prefs.exhaustivity == AppExhaustivity.ENHANCED,
            onCheckedChange = { isChecked ->
                viewModel.updateExhaustivity(isChecked)
            }
        )

        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun RowScope.ColorOptionItem(
    color: Color,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .weight(1f)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
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
            style = MaterialTheme.typography.bodySmall,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) PosdataBlackText else PosdataMutedText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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