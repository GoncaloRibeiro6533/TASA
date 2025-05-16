package com.tasa.newlocation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tasa.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import org.osmdroid.config.Configuration

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, prefs)

        setContent {
            TasaTheme {
                MapScreen(
                    onNavigationBack = { navigateTo(this, HomePageActivity::class.java) },
                )
            }
        }
    }
}
