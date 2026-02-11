package com.posdata.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.posdata.app.model.UserData
import com.posdata.app.ui.theme.*

@Composable
fun MainScreen(userData: UserData?) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            PosdataMainBottomBar(navController = navController)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeContent(
                    fullName = userData?.fullName ?: "Usuario",
                    analyzedSms = 124,
                    fraudSms = 3
                )
            }

            composable(Screen.Profile.route) {
                ProfileContent(
                    userData = userData
                )
            }

            composable(Screen.Contacts.route) {
                ContactsContent(
                    userData = userData
                )
            }

            composable(Screen.Settings.route) {
                SettingsContent(
                    userData = userData
                )
            }
        }
    }
}

@Composable
fun PosdataMainBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 10.dp,
        modifier = Modifier.height(110.dp)
    ) {
        // --- HOME ---
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Home, "Inicio", Modifier.size(32.dp)) },
            label = { Text("Inicio", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PosdataBlue,
                selectedTextColor = PosdataBlue,
                indicatorColor = PosdataLightBlue.copy(alpha = 0.3f)
            )
        )

        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                navController.navigate(Screen.Profile.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Person, "Perfil", Modifier.size(32.dp)) },
            label = { Text("Perfil", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PosdataBlue,
                selectedTextColor = PosdataBlue,
                indicatorColor = PosdataLightBlue.copy(alpha = 0.3f)
            )
        )

        // --- CONTACTOS ---
        NavigationBarItem(
            selected = currentRoute == Screen.Contacts.route,
            onClick = {
                navController.navigate(Screen.Contacts.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Groups, "Contactos", Modifier.size(32.dp)) },
            label = { Text("Contactos", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PosdataBlue,
                selectedTextColor = PosdataBlue,
                indicatorColor = PosdataLightBlue.copy(alpha = 0.3f)
            )
        )

        // --- AJUSTES ---
        NavigationBarItem(
            selected = currentRoute == Screen.Settings.route,
            onClick = {
                navController.navigate(Screen.Settings.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            icon = { Icon(Icons.Filled.Settings, "Ajustes", Modifier.size(32.dp)) },
            label = { Text("Ajustes", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = PosdataBlue,
                selectedTextColor = PosdataBlue,
                indicatorColor = PosdataLightBlue.copy(alpha = 0.3f)
            )
        )
    }
}