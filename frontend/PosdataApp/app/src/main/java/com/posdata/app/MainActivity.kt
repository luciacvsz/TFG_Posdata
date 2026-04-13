package com.posdata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.posdata.app.data.local.UserDataStore
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.model.UserData
import com.posdata.app.sms.SMSReceiverEnabler
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

/**
 * Main entry point of the application.
 *
 * Responsible for:
 * - Initializing [RetrofitClient] with the API key and URLs from `local.env`.
 * - Instantiating all ViewModels at the activity level so they survive
 *   recompositions and are shared across screens.
 * - Observing [UserDataStore.userData] to reactively apply the user's
 *   color scheme and font size preferences to the app theme.
 * - Delegating navigation and screen rendering to [AppContent].
 */
class MainActivity : ComponentActivity() {

    private lateinit var userDataStore: UserDataStore
    private lateinit var preferencesViewModel: PreferencesViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(this)

        userDataStore = UserDataStore(applicationContext)

        preferencesViewModel = ViewModelProvider(
            this,
            PreferencesViewModelFactory(applicationContext)
        )[PreferencesViewModel::class.java]

        setContent {
            val userData by userDataStore.userData.collectAsState(initial = null)
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
                        userData,
                        preferencesViewModel
                    )
                }
            }
        }

    }
}

/**
 * Root composable that drives the top-level navigation of the app.
 *
 * Shows a loading indicator while [userData] is null (initial DataStore read).
 * Once loaded, navigates to the main flow if [UserData.isLoggedIn] is true,
 * or to the login screen otherwise.
 *
 * Navigation after logout or account deletion is fully automatic — when
 * [UserDataStore.userData] emits a new value with [isLoggedIn] = false,
 * this composable recomposes and redirects to the login screen without
 * any explicit navigation call from the ViewModels.
 *
 * The login and register success callbacks are intentionally empty for the
 * same reason: session state changes in the DataStore drive navigation,
 * not imperative callback calls.
 *
 * Observes [UserData.isLoggedIn] via [LaunchedEffect] to enable or disable
 * [SMSReceiverEnabler] whenever the session state changes — including on app
 * restart with an existing session, where neither login nor registration
 * flows are triggered.
 *
 * @param userData Current session data observed from [UserDataStore], or null while loading.
 * @param preferencesViewModel Shared ViewModel passed down to [MainScreen].
 */
@Composable
private fun AppContent(
    userData: UserData?,
    preferencesViewModel: PreferencesViewModel
) {
    val rootNavController = rememberNavController()
    val context = LocalContext.current

    LaunchedEffect(userData?.isLoggedIn) {
        if (userData?.isLoggedIn == true) {
            SMSReceiverEnabler.enableReceiver(context)
        } else if (userData?.isLoggedIn == false) {
            SMSReceiverEnabler.disableReceiver(context)
        }
    }

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
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(LocalContext.current)
                )

                LoginScreen(
                    viewModel = loginViewModel,
                    onRegisterClick = {
                        rootNavController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {}
                )
            }

            composable(Screen.Register.route) {
                val registerViewModel: RegisterViewModel = viewModel(
                    factory = RegisterViewModelFactory(LocalContext.current)
                )

                RegisterScreen(
                    viewModel = registerViewModel,
                    onBackClick = {
                        rootNavController.popBackStack()
                    },
                    onRegisterSuccess = {}
                )
            }

            composable("main_flow") {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(LocalContext.current)
                )

                val trustedContactsViewModel: TrustedContactsViewModel = viewModel(
                    factory = TrustedContactsViewModelFactory(LocalContext.current)
                )

                MainScreen(
                    userData,
                    profileViewModel,
                    trustedContactsViewModel,
                    preferencesViewModel)
            }
        }
    }
}