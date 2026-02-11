package com.posdata.app

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Contacts : Screen("contacts")
    object Settings : Screen("settings")
    object Login : Screen("login")
    object Register : Screen("register")
}