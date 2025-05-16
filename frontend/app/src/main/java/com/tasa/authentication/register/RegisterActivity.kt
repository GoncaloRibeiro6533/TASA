package com.tasa.authentication.register

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasa.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TasaTheme {
                RegisterScreen(
                    onNavigationBack = { navigateTo(this, StartActivity::class.java) },
                )
            }
        }
    }
}
