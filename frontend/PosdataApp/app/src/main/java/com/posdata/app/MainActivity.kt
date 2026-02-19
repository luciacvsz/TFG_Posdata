package com.posdata.app

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PackageManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.posdata.app.data.local.UserInfo
import com.posdata.app.model.UserData
import com.posdata.app.sms.SMSReceiverManager
import com.posdata.app.ui.theme.PosdataAppTheme
import com.posdata.app.ui.screens.login.LoginScreen
import com.posdata.app.ui.screens.home.MainScreen
import com.posdata.app.ui.screens.register.RegisterScreen
import com.posdata.app.ui.navigation.Screen
import com.posdata.app.ui.screens.login.LoginViewModel
import com.posdata.app.ui.screens.login.LoginViewModelFactory
import com.posdata.app.ui.screens.preferences.PreferencesViewModel
import com.posdata.app.ui.screens.preferences.PreferencesViewModelFactory
import com.posdata.app.ui.screens.profile.ProfileViewModel
import com.posdata.app.ui.screens.profile.ProfileViewModelFactory
import com.posdata.app.ui.screens.register.RegisterViewModel
import com.posdata.app.ui.screens.register.RegisterViewModelFactory
import com.posdata.app.ui.screens.trusted_contacts.TrustedContactsViewModel
import com.posdata.app.ui.screens.trusted_contacts.TrustedContactsViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var userInfo: UserInfo
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var registerViewModel: RegisterViewModel
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var trustedContactsViewModel: TrustedContactsViewModel
    private lateinit var preferencesViewModel: PreferencesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        userInfo = UserInfo(applicationContext)

        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(applicationContext)
        )[LoginViewModel::class.java]

        registerViewModel = ViewModelProvider(
            this,
            RegisterViewModelFactory(applicationContext)
        )[RegisterViewModel::class.java]

        profileViewModel = ViewModelProvider(
            this,
            ProfileViewModelFactory(applicationContext)
        )[ProfileViewModel::class.java]

        trustedContactsViewModel = ViewModelProvider(
            this,
            TrustedContactsViewModelFactory(applicationContext)
        )[TrustedContactsViewModel::class.java]

        preferencesViewModel = ViewModelProvider(
            this,
            PreferencesViewModelFactory(applicationContext)
        )[PreferencesViewModel::class.java]

        setContent {
            val userData by userInfo.userData.collectAsState(initial = null)
            val currentColorScheme by preferencesViewModel.currentColorScheme.collectAsState()
            val currentFontSize by preferencesViewModel.currentFontSize.collectAsState()
            PosdataAppTheme(
                colorScheme = currentColorScheme,
                fontSize = currentFontSize
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppContent(
                        userData = userData,
                        preferencesViewModel = preferencesViewModel
                    )
                }
            }
        }

    }
}

@Composable
private fun AppContent(
    userData: UserData?,
    preferencesViewModel: PreferencesViewModel
) {
    val rootNavController = rememberNavController()

    if(userData == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = rootNavController,
            startDestination = if (userData.isLoggedIn) "main_flow" else Screen.Login.route
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onRegisterClick = {
                        rootNavController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {}
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onBackClick = {
                        rootNavController.popBackStack()
                    },
                    onRegisterSuccess = {}
                )
            }

            composable("main_flow") {
                MainScreen(userData = userData, preferencesViewModel)
            }
        }
    }
}