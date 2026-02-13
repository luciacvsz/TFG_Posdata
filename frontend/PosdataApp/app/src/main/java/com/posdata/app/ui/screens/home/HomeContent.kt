package com.posdata.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.posdata.app.ui.components.PosdataPrimaryButton
import com.posdata.app.ui.components.PosdataStatCard
import com.posdata.app.ui.theme.*

@Composable
fun HomeContent(
    fullName: String,
    analyzedSms: Int,
    fraudSms: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "¡Hola, $fullName!",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            PosdataStatCard(
                number = analyzedSms.toString(),
                label = "SMS Analizados",
                colorStart = MaterialTheme.colorScheme.primaryContainer,
                colorEnd = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(170.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            PosdataStatCard(
                number = fraudSms.toString(),
                label = "Amenazas\nBloqueadas",
                colorStart = MaterialTheme.colorScheme.primaryContainer,
                colorEnd = MaterialTheme.colorScheme.primary,
                modifier = Modifier.height(170.dp)
            )
        }

        Spacer(modifier = Modifier.height(26.dp))

        Column(
            modifier = Modifier
                .padding(horizontal = 36.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Necesitas ayuda urgente?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            PosdataPrimaryButton(
                text = "Llamada de Emergencia",
                onClick = { /* Lógica 112 */ },
                modifier = Modifier.height(90.dp),
                colorOverride = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}