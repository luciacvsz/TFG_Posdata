package com.posdata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.posdata.app.data.local.UserInfo
import com.posdata.app.ui.theme.PosdataAppTheme
import com.posdata.app.ui.screens.login.LoginScreen
import com.posdata.app.ui.screens.home.MainScreen
import com.posdata.app.ui.screens.register.RegisterScreen
import com.posdata.app.ui.navigation.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userInfo = UserInfo(applicationContext)

        setContent {
            PosdataAppTheme {
                val userDataState = userInfo.userData.collectAsState(initial = null)
                val userData = userDataState.value

                val authNavController = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when {
                        userData == null -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        userData.isLoggedIn -> {
                            MainScreen(userData = userData)
                        }
                        else -> {
                            NavHost(
                                navController = authNavController,
                                startDestination = Screen.Login.route
                            ) {
                                composable(Screen.Login.route) {
                                    LoginScreen(
                                        onRegisterClick = {
                                            authNavController.navigate(Screen.Register.route)
                                        },
                                        onLoginSuccess = {
                                            authNavController.navigate(Screen.Home.route)
                                        }
                                    )
                                }

                                composable(Screen.Register.route) {
                                    RegisterScreen(
                                        onBackClick = {
                                            authNavController.popBackStack()
                                        },
                                        onRegisterSuccess = {
                                            authNavController.navigate(Screen.Home.route)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}