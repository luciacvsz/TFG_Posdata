package com.posdata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.posdata.app.ui.theme.PosdataAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PosdataAppTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate("main_container") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onRegisterClick = { /* Lógica de registro */ }
                        )
                    }

                    composable("main_container") {
                        MainScreenContainer()
                    }
                }
            }
        }
    }
}