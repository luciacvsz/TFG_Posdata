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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.posdata.app.ui.theme.*

@Composable
fun ContactsScreen() {
    Scaffold(
        bottomBar = {
            ContactsBottomBar()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        // COLUMNA PRINCIPAL (Sin scroll global)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- BLOQUE SUPERIOR (LISTA CON SCROLL) ---
            // Este bloque ocupa todo el espacio disponible (weight 1f)
            Column(
                modifier = Modifier
                    .weight(1f) // El muelle que empuja el botón abajo
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp), // Margen lateral para la lista
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(32.dp))

                // TÍTULO
                Text(
                    text = "Contactos de\nConfianza",
                    style = MaterialTheme.typography.headlineLarge,
                    color = PosdataBlackText,
                    textAlign = TextAlign.Start,
                    lineHeight = 60.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // LISTA DE CONTACTOS
                ContactCard(
                    name = "María Pérez",
                    relation = "Hija",
                    phone = "+34 100 000 000",
                    email = "hija@correo.com",
                    onClick = { /* Ver detalle */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ContactCard(
                    name = "Carlos Gómez",
                    relation = "Nieto",
                    phone = "+34 200 000 000",
                    onClick = { /* Ver detalle */ }
                )

                Spacer(modifier = Modifier.height(16.dp))

                ContactCard(
                    name = "Fina López",
                    relation = "Vecina",
                    email = "vecina@correo.com",
                    onClick = { /* Ver detalle */ }
                )

                // Espacio extra al final de la lista para que no se corte con el botón
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- BLOQUE INFERIOR (BOTÓN FIJO) ---
            // Está fuera del scroll, pegado abajo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp) // Mismo margen que la lista
                    .padding(top = 16.dp, bottom = 24.dp) // Margen para respirar
            ) {
                AddContactButton(onClick = { /* Lógica para añadir */ })
            }
        }
    }
}

// --- COMPONENTE BOTÓN AÑADIR (ACTUALIZADO: Fondo Blanco) ---
@Composable
fun AddContactButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            // 1. Damos forma y cortamos para que el fondo no se salga
            .clip(RoundedCornerShape(20.dp))
            // 2. APLICAMOS EL FONDO BLANCO
            .background(Color.White)
            // 3. Dibujamos el borde punteado POR ENCIMA del fondo blanco
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
            // Icono + opcional (si quieres añadir el icono +)
            // Text "+ Añadir Contacto"
            Text(
                text = "+ Añadir Contacto",
                style = MaterialTheme.typography.labelLarge, // Sora Bold
                color = PosdataLightBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ... (El resto de componentes: ContactCard y ContactsBottomBar se mantienen igual) ...
// Copia aquí abajo tus funciones ContactCard y ContactsBottomBar del mensaje anterior
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

@Composable
fun ContactsBottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 10.dp,
        modifier = Modifier.height(110.dp)
    ) {
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Home, "Inicio", Modifier.size(42.dp)) },
            label = { Text("Inicio", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Person, "Perfil", Modifier.size(42.dp)) },
            label = { Text("Perfil", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )
        NavigationBarItem(
            selected = true,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Groups, "Contactos", Modifier.size(42.dp)) },
            label = { Text("Contactos", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(selectedIconColor = PosdataBlue, selectedTextColor = PosdataBlue, indicatorColor = PosdataLightBlue.copy(alpha = 0.3f))
        )
        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = { Icon(Icons.Filled.Settings, "Ajustes", Modifier.size(42.dp)) },
            label = { Text("Ajustes", fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = PosdataMutedText, unselectedTextColor = PosdataMutedText)
        )
    }
}