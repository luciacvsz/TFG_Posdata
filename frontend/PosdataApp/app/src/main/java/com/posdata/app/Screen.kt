package com.posdata.app

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Settings

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Contacts : Screen("contacts", "Contactos", Icons.Default.Person)
    object Profile : Screen("profile", "Perfil", Icons.Default.AccountCircle)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
}