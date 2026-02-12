package com.posdata.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Profile : Screen("profile")
    object TrustedContacts : Screen("trustedContacts")
    object Preferences : Screen("preferences")
    object Login : Screen("login")
    object Register : Screen("register")
}