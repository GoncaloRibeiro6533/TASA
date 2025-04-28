package com.tasa.start

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasa.authentication.login.LoginActivity
import com.tasa.authentication.register.RegisterActivity
import com.tasa.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class StartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TasaTheme {
                StartScreen(
                    onLoginRequested = {navigateTo(this, LoginActivity::class.java)},
                    onRegisterRequested = {navigateTo(this, RegisterActivity::class.java)}
                )
            }
        }
    }
}