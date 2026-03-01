package com.posdata.app.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.posdata.app.ui.components.PosdataPrimaryButton
import androidx.core.net.toUri

/**
 * Main content of the Home screen.
 *
 * Displays a personalized greeting and quick-access emergency contact buttons,
 * allowing the user to call emergency services (112) or the national
 * cybersecurity helpline (017 - INCIBE) directly from the app.
 *
 * @param fullName Full name of the authenticated user, shown in the greeting.
 */
@Composable
fun HomeContent(
    fullName: String
) {
    val context = LocalContext.current

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

            Text(
                "¿Necesitas ayuda urgente?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Emergency call button — opens the dialer with 112 pre-filled
            PosdataPrimaryButton(
                text = "Llamar al 112 (Emergencias)",
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:112".toUri()
                    }
                    context.startActivity(intent)
                },
                colorOverride = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Cybersecurity helpline button — opens the dialer with 017 pre-filled
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = "tel:017".toUri()
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Ayuda en Ciberseguridad",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "017 (INCIBE)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}