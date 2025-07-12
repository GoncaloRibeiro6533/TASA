package com.tasa.ui.screens.newLocation

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import com.tasa.DependenciesContainer
import com.tasa.domain.Location
import com.tasa.location.LocationService
import com.tasa.ui.screens.mylocations.MyLocationsActivity
import com.tasa.ui.screens.start.StartActivity
import com.tasa.ui.theme.TasaTheme
import com.tasa.utils.navigateTo
import kotlin.jvm.java

class MapActivity : ComponentActivity() {
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private val repo by lazy { (application as DependenciesContainer).repo }

    private val locationManager by lazy {
        (application as DependenciesContainer).locationUpdatesRepository
    }

    private val searchPlaceService by lazy {
        (application as DependenciesContainer).searchPlaceService
    }

    private val stringResolver by lazy {
        (application as DependenciesContainer).stringResourceResolver
    }

    private val userInfoRepository by lazy {
        (application as DependenciesContainer).userInfoRepository
    }

    private val geofenceManager by lazy {
        (application as DependenciesContainer).geofenceManager
    }

    private val serviceKiller by lazy {
        (application as DependenciesContainer).serviceKiller
    }
    private val alarmScheduler by lazy {
        (application as DependenciesContainer).ruleScheduler
    }
    private val viewModel by viewModels<MapScreenViewModel>(
        factoryProducer = {
            MapScreenViewModelFactory(
                repo = repo,
                locationClient = fusedLocationClient,
                locationUpdatesRepository = locationManager,
                searchPlaceService = searchPlaceService,
                stringResolver = stringResolver,
                userInfo = userInfoRepository,
                geofenceManager = geofenceManager,
                serviceKiller = serviceKiller,
                alarmScheduler = alarmScheduler,
            )
        },
    )

    val fakeLoc =
        Location(
            id = 0,
            name = "loc",
            latitude = 0.0,
            longitude = 0.0,
            radius = 1.0,
        )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        Log.e("MapActivity", "Activity started")

        val origin = intent.getStringExtra("origin")
        Log.e("MapActivity", "Origin: $origin")

        val location =
            try {
                intent.getParcelableExtra("location", Location::class.java)
            } catch (e: Exception) {
                Log.e("MapActivity", "Failed to get location: ${e.message}")
                null
            }
        if (location == null && origin == "FromMyLocations") {
            navigateTo(this, MyLocationsActivity::class.java)
            finish()
            return
        }

        viewModel.keepGivenCurrentLocation()

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
                        viewModel.getLocationFromSearchQuery()
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
                        )
                    },
                    onRecenterMap = {
                        viewModel.recenterMap()
                    },
                    onLocationsIntent = {
                        navigateTo(
                            this@MapActivity,
                            MyLocationsActivity::class.java,
                        )
                        finish()
                    },
                    onSessionExpired = {
                        finishAffinity()
                        navigateTo(
                            this@MapActivity,
                            StartActivity::class.java,
                        )
                    },
                    onEditCenterButton = { _, name, radius, latitude, longitude ->
                        viewModel.onChangeCenter(
                            location = location ?: fakeLoc,
                            locationName = name,
                            radius = radius,
                            latitude = latitude,
                            longitude = longitude,
                        )
                    },
                    previousLocation = location ?: fakeLoc,
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
