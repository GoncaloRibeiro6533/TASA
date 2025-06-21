package com.tasa.ui.screens.mylocations

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
import kotlin.jvm.java

class MyLocationsActivity : ComponentActivity() {
    private val repo by lazy {
        (applicationContext as DependenciesContainer).repo
    }

    private val geofenceManager by lazy {
        (applicationContext as DependenciesContainer).geofenceManager
    }

    private val viewModel by viewModels<MyLocationsScreenViewModel>(
        factoryProducer = {
            MyLocationsScreenViewModelFactory(
                repo = repo,
                geofenceManager = geofenceManager,
            )
        },
    )

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadLocations()
        setContent {
            TasaTheme {
                MyLocationsScreen(
                    viewModel = viewModel,
                    onLocationSelected = {},
                    onAddLocation = {},
                    onDeleteLocation = {},
                    onEditLocation = {},
                    onNavigateBack = {
                        navigateTo(this, HomePageActivity::class.java)
                        finish()
                    },
                    onCreateRuleLocation = { location, startTime, endTime ->
                        viewModel.createRulesForLocation(location, startTime, endTime)
                    },
                    onSetCreateRuleState = {
                            location ->
                        viewModel.setCreatingRuleLocationState(location)
                    },
                    onSetSuccessState = {
                        viewModel.setSuccessState()
                    },
                )
            }
        }
    }
}
