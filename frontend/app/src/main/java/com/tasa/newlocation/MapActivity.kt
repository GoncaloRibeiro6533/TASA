package com.tasa.newlocation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import org.osmdroid.config.Configuration

class MapActivity : ComponentActivity() {

    private val service by lazy {(application as DependenciesContainer).service }

    private val viewModel by viewModels<MapScreenViewModel>(
        factoryProducer = {
            MapScreenViewModelFactory(
                locationService = service.locationService
            )
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = applicationContext.getSharedPreferences("osmdroid", MODE_PRIVATE)
        Configuration.getInstance().load(applicationContext, prefs)

        setContent {
            TasaTheme @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION]) {
                MapScreen(
                    viewModel = viewModel,
                    onNavigationBack = { navigateTo(this, HomePageActivity::class.java) },
                    onAddedLocation = {finish()}
                )
            }
        }
    }
}
