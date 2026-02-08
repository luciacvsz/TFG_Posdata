package com.posdata.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun HomeScreen(
    userName: String,
    analyzedSms: Int,
    fraudSms: Int
) {
    Scaffold(
        bottomBar = {
            PosdataBottomBar()
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    text = "¡Hola, $userName!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = PosdataBlackText,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                StatCard(
                    number = analyzedSms.toString(),
                    label = "SMS Analizados",
                    colorStart = PosdataLightBlue,
                    colorEnd = PosdataBlue,
                    modifier = Modifier.height(170.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                StatCard(
                    number = fraudSms.toString(),
                    label = "Amenazas\nBloqueadas",
                    colorStart = PosdataLightBlue,
                    colorEnd = PosdataBlue,
                    modifier = Modifier.height(170.dp) // <--- CAMBIO CLAVE: Más alta
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Column(
                modifier = Modifier
                    .padding(horizontal = 36.dp)
                    .padding(bottom = 24.dp), // Margen inferior
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "¿Necesitas ayuda urgente?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PosdataMutedText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = { /* Lógica de llamada 112 */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PosdataRed,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Llamada de Emergencia", // Texto en una línea si cabe, o dos
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    number: String,
    label: String,
    colorStart: Color,
    colorEnd: Color,
    modifier: Modifier = Modifier.height(140.dp)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(colors = listOf(colorStart, colorEnd)),
                shape = RoundedCornerShape(28.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = number,
                fontFamily = SoraFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 60.sp, // Número un poco más grande
                color = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PosdataBottomBar() {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 10.dp,
        modifier = Modifier.height(110.dp)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { /* Navegar */ },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Inicio",
                    modifier = Modifier.size(42.dp)
                )
            },
            label = {
                Text("Inicio", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PosdataBlue,
                selectedTextColor = PosdataBlue,
                indicatorColor = PosdataLightBlue.copy(alpha = 0.3f)
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Perfil",
                    modifier = Modifier.size(42.dp)
                )
            },
            label = {
                Text("Perfil", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = PosdataMutedText,
                unselectedTextColor = PosdataMutedText
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Groups,
                    contentDescription = "Contactos",
                    modifier = Modifier.size(42.dp)
                )
            },
            label = {
                Text("Contactos", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = PosdataMutedText,
                unselectedTextColor = PosdataMutedText
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = { /* Navegar */ },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Ajustes",
                    modifier = Modifier.size(42.dp)
                )
            },
            label = {
                Text("Ajustes", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            },
            colors = NavigationBarItemDefaults.colors(
                unselectedIconColor = PosdataMutedText,
                unselectedTextColor = PosdataMutedText
            )
        )
    }
}