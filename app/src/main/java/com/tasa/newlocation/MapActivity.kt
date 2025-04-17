package com.tasa.newlocation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import org.osmdroid.config.Configuration



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, prefs)


        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}
