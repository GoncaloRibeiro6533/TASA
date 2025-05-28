package com.tasa.newevent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class NewEvenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TasaTheme {
                NewEventScreen(
                    onNavigationBack = { navigateTo(this, HomePageActivity::class.java) },
                )
            }
        }
    }
}
