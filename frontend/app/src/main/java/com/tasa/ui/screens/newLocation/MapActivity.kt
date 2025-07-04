package com.tasa.ui.screens.newLocation

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.tasa.DependenciesContainer
import com.tasa.activity.UserActivityTransitionManager
import com.tasa.location.LocationService
import com.tasa.ui.screens.mylocations.MyLocationsActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import kotlinx.coroutines.launch
import kotlin.jvm.java

class MapActivity : ComponentActivity() {
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val repo by lazy { (application as DependenciesContainer).repo }

    private val userInfoRepository by lazy { (application as DependenciesContainer).userInfoRepository }

    private val activityRecognitionManager by lazy {
        UserActivityTransitionManager(application)
    }

    private val locationManager by lazy {
        (application as DependenciesContainer).locationUpdatesRepository
    }

    private val viewModel by viewModels<MapScreenViewModel>(
        factoryProducer = {
            MapScreenViewModelFactory(
                repo = repo,
                userInfo = userInfoRepository,
                locationClient = fusedLocationClient,
                activityRecognitionManager = activityRecognitionManager,
                locationUpdatesRepository = locationManager,
            )
        },
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
        ],
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            viewModel.keepGivenCurrentLocation(this@MapActivity)
        }
        setContent {
            TasaTheme {
                MapScreen(
                    viewModel = viewModel,
                    onNavigationBack = {
                        finish()
                    },
                    onLocationSelected = { geoPoint ->
                        viewModel.updateSelectedPoint(geoPoint)
                    },
                    onSearchQuery = {
                        viewModel.getLocationFromSearchQuery(
                            this@MapActivity,
                        )
                    },
                    onUpdateRadius = { it ->
                        viewModel.updateRadius(it)
                    },
                    onUpdateLocationName = { name ->
                        viewModel.editLocationName(name)
                    },
                    onEditSearchBox = { query ->
                        viewModel.updateSearchQuery(query)
                    },
                    onCreateLocationButton = {
                        viewModel.setEditingLocationState()
                    },
                    onDismissEditingLocation = {
                        viewModel.onDismissEditingLocation()
                    },
                    onConfirmEditingLocation = { name, radius, latitude, longitude ->
                        viewModel.onCreateLocation(
                            locationName = name,
                            radius = radius,
                            latitude = latitude,
                            longitude = longitude,
                        ) {
                            navigateTo(
                                this@MapActivity,
                                MyLocationsActivity::class.java,
                            )
                            finish()
                        }
                    },
                    onTouchSearchBox = {
                        viewModel.setSearchingState()
                    },
                    onUnTouchSearchBox = {
                        viewModel.setUnSearchingState()
                    },
                    onRecenterMap = {
                        viewModel.recenterMap()
                    },
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // check if the service is running before stopping location updates
        if (!LocationService.isRunning) viewModel.stopLocationUpdates()
    }
}
