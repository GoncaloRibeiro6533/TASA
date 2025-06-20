package com.tasa.ui.screens.newLocation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.LocationServices
import com.tasa.DependenciesContainer
import com.tasa.newlocation.UserActivityTransitionManager
import com.tasa.ui.components.PermissionBox
import com.tasa.ui.screens.homepage.HomePageActivity
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
        (application as DependenciesContainer).locationManager
    }

    private val viewModel by viewModels<MapScreenViewModel>(
        factoryProducer = {
            MapScreenViewModelFactory(
                repo = repo,
                userInfo = userInfoRepository,
                locationClient = fusedLocationClient,
                activityRecognitionManager = activityRecognitionManager,
                locationManager = locationManager,
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
            viewModel.keepGivenCurrentLocation()
        }
        lifecycleScope.launch {
            viewModel.getActivityState()
        }
        setContent {
            TasaTheme {
                val activityPermission =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Manifest.permission.ACTIVITY_RECOGNITION
                    } else {
                        "com.google.android.gms.permission.ACTIVITY_RECOGNITION"
                    }

                val permissions =
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        activityPermission,
                    )

                PermissionBox(
                    permissions = permissions,
                    requiredPermissions = permissions,
                    onGranted = @RequiresPermission(
                        allOf =
                            [
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACTIVITY_RECOGNITION,
                                "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
                            ],
                    ) {
                        MapScreen(
                            viewModel = viewModel,
                            onNavigationBack = {
                                navigateTo(this@MapActivity, HomePageActivity::class.java)
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
                            onRequestLocationUpdates = {
                                viewModel.restartLocationUpdates()
                            },
                            onMapReady = {
                                viewModel.notifyMapReady()
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
                            onConfirmEditingLocation = {
                                    name, radius, latitude, longitude ->
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
                        )
                    },
                )
            }
        }
    }

    private fun requestActivityRecognitionPermission(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    1001, // Código de solicitação
                )
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    override fun onDestroy() {
        super.onDestroy()
        viewModel.locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}
