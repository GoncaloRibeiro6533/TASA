package com.tasa.newlocation

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import com.tasa.DependenciesContainer
import com.tasa.ui.screens.homepage.HomePageActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo

class MapActivity : ComponentActivity() {
    private val service by lazy { (application as DependenciesContainer).service }

    private val viewModel by viewModels<MapScreenViewModel>(
        factoryProducer = {
            MapScreenViewModelFactory(
                locationService = service.locationService,
            )
        },
    )

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ],
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions =
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        setContent {
            TasaTheme {
                /*PermissionBox(
                    permissions = permissions,
                    requiredPermissions = permissions,
                    onGranted = @RequiresPermission(
                        allOf =
                            [
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ],
                    ) {*/
                MapScreen(
                    viewModel = viewModel,
                    onNavigationBack = { navigateTo(this@MapActivity, HomePageActivity::class.java) },
                    onAddedLocation = { finish() },
                )
                // }
            }
        }
    }
}
