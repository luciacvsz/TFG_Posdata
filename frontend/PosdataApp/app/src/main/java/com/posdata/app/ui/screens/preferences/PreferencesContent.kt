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
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.posdata.app.model.*
import com.posdata.app.ui.components.PosdataColorOptionSwitch
import com.posdata.app.ui.components.PosdataPreferenceSwitch
import com.posdata.app.ui.components.PosdataStatusDialog
import com.posdata.app.ui.screens.profile.ProfileUiState
import com.posdata.app.ui.screens.trusted_contacts.TrustedContactsUiState
import com.posdata.app.ui.theme.*

@Composable
fun PreferencesContent(
    userData: UserData?,
    viewModel: PreferencesViewModel = viewModel(
        factory = PreferencesViewModelFactory(LocalContext.current)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val preferences = userData?.preferences ?: AppPreferences()
    val isLoading = uiState is PreferencesUiState.Loading

    if (uiState is PreferencesUiState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)).zIndex(1f),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }

    when (val state = uiState) {
        is PreferencesUiState.Success -> {
            PosdataStatusDialog(
                message = state.message,
                isSuccess = true,
                onDismiss = { viewModel.resetState() }
            )
        }
        is PreferencesUiState.Error -> {
            PosdataStatusDialog(
                message = state.message,
                isSuccess = false,
                onDismiss = { viewModel.resetState() }
            )
        }
        else -> {}
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
            text = "Preferencias",
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
            PosdataColorOptionSwitch(
                color = PosdataBlue,
                label = "Estándar",
                isSelected = preferences.colorScheme == AppColorScheme.STANDARD,
                onClick = { viewModel.updateColorScheme(AppColorScheme.STANDARD) }
            )
            PosdataColorOptionSwitch(
                color = Color.Black,
                label = "Contraste",
                isSelected = preferences.colorScheme == AppColorScheme.HIGH_CONTRAST,
                onClick = { viewModel.updateColorScheme(AppColorScheme.HIGH_CONTRAST) }
            )
            PosdataColorOptionSwitch(
                color = Color(0xFF8E44AD), // Violeta (distinguible para Protanopia)
                label = "Protanopia",
                isSelected = preferences.colorScheme == AppColorScheme.PROTANOPIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.PROTANOPIA) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PosdataColorOptionSwitch(
                color = Color(0xFFD35400),
                label = "Deutera...",
                isSelected = preferences.colorScheme == AppColorScheme.DEUTERANOPIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.DEUTERANOPIA) }
            )
            PosdataColorOptionSwitch(
                color = Color(0xFF16A085),
                label = "Tritanopia",
                isSelected = preferences.colorScheme == AppColorScheme.TRITANOPIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.TRITANOPIA) }
            )
            PosdataColorOptionSwitch(
                color = Color.Gray,
                label = "Acroma...",
                isSelected = preferences.colorScheme == AppColorScheme.ACHROMATOPSIA,
                onClick = { viewModel.updateColorScheme(AppColorScheme.ACHROMATOPSIA) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        HorizontalDivider(color = PosdataGreyBorder)
        Spacer(modifier = Modifier.height(32.dp))

        PosdataPreferenceSwitch(
            label = "Texto\nGrande",
            isChecked = preferences.fontSize == AppFontSize.LARGE,
            onCheckedChange = { isChecked ->
               viewModel.updateFontSize(isChecked)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        PosdataPreferenceSwitch(
            label = "Sonido de\nNotificación",
            isChecked = preferences.notificationSound == AppNotificationSound.ON,
            onCheckedChange = { isChecked ->
                viewModel.updateNotificationSound(isChecked)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        PosdataPreferenceSwitch(
            label = "Explicaciones\ndetalladas",
            isChecked = preferences.explanationMode == AppExplanationMode.ON,
            onCheckedChange = { isChecked ->
                viewModel.updateExplanationMode(isChecked)            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        PosdataPreferenceSwitch(
            label = "Análisis\nExhaustivo",
            isChecked = preferences.exhaustivity == AppExhaustivity.ENHANCED,
            onCheckedChange = { isChecked ->
                viewModel.updateExhaustivity(isChecked)
            }
        )

        Spacer(modifier = Modifier.height(50.dp))
    }
}

