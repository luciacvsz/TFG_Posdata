package com.posdata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.posdata.app.data.UserInfo
import com.posdata.app.ui.theme.PosdataAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userInfo = UserInfo(applicationContext)

        setContent {
            PosdataAppTheme {
                val userDataState = userInfo.userData.collectAsState(initial = null)
                val userData = userDataState.value

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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
                            LoginScreen()
                        }
                    }
                }
            }
        }
    }
}