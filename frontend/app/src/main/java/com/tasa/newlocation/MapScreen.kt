package com.tasa.newlocation

import android.Manifest
import android.location.Geocoder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tasa.authentication.register.SuccessView
import com.tasa.domain.Location
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.Locale
import java.util.concurrent.TimeUnit

@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel,
    onNavigationBack: () -> Unit,
    onAddedLocation: () -> Unit,
) {
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }

    if (!permissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Permissão de localização é necessária para usar o mapa.")
        }
        return
    }

    val mapState = viewModel.state.collectAsState().value

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }

    var editingLocation by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var formInput by remember { mutableStateOf("") }
    var radiusMeters by remember { mutableStateOf(100.0) }

    var locationRequest by remember {
        mutableStateOf<LocationRequest?>(
            LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                TimeUnit.SECONDS.toMillis(3),
            ).setMinUpdateDistanceMeters(3f)
                .setWaitForAccurateLocation(true)
                .build(),
        )
    }

    var accuracy by remember { mutableStateOf(0f) }
    locationRequest?.let {
        LocationUpdatesEffect(it) { result ->
            for (location in result.locations) {
                val point = GeoPoint(location.latitude, location.longitude)
                if (selectedPoint != point && (accuracy > location.accuracy || accuracy.toInt() == 0)) {
                    selectedPoint = point
                    accuracy = location.accuracy
                }
                currentLocation = point
            }
        }
    }

    TasaTheme {
        Scaffold(
            topBar = {
                TopBar(NavigationHandlers(onBackRequested = onNavigationBack))
            },
        ) { innerPadding ->

            when (mapState) {
                is MapScreenState.Idle -> {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    ) {
                        // Map
                        OSMDroidMap(
                            modifier = Modifier.fillMaxSize(),
                            center = selectedPoint ?: GeoPoint(38.7169, -9.1399),
                            circleCenter = if (showDialog && selectedPoint != null) selectedPoint else null,
                            circleRadius = if (showDialog) radiusMeters else null,
                            currentLocation = currentLocation,
                            onCoordinateSelected = { point ->
                                selectedPoint = point
                                editingLocation = false
                            },
                        )

                        // Search UI
                        Surface(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                            tonalElevation = 4.dp,
                            shadowElevation = 8.dp,
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surface,
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Search for a place") },
                                    modifier = Modifier.fillMaxWidth(),
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            try {
                                                val geocoder = Geocoder(context, Locale.getDefault())
                                                val addresses =
                                                    withContext(Dispatchers.IO) {
                                                        geocoder.getFromLocationName(searchQuery, 1)
                                                    }
                                                if (!addresses.isNullOrEmpty()) {
                                                    val address = addresses[0]
                                                    selectedPoint = GeoPoint(address.latitude, address.longitude)
                                                    editingLocation = false
                                                } else {
                                                    Log.d("MapScreen", "No results for $searchQuery")
                                                }
                                            } catch (e: Exception) {
                                                Log.e("MapScreen", "Geocoding error: ${e.message}")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text("Search")
                                }
                            }
                        }

                        if (selectedPoint != null && !editingLocation) {
                            Button(
                                onClick = {
                                    editingLocation = true
                                    showDialog = true
                                    radiusMeters = 100.0
                                },
                                modifier =
                                    Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(16.dp),
                            ) {
                                Text("Add Location")
                            }
                        }

                        // Dialog with form
                        if (showDialog) {
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                contentAlignment = Alignment.BottomEnd,
                            ) {
                                Surface(
                                    shape = MaterialTheme.shapes.medium,
                                    tonalElevation = 8.dp,
                                    shadowElevation = 12.dp,
                                    modifier = Modifier.width(280.dp),
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                    ) {
                                        Text("Enter info for this location:")
                                        OutlinedTextField(
                                            value = formInput,
                                            onValueChange = { formInput = it },
                                            label = { Text("Description") },
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        OutlinedTextField(
                                            value = radiusMeters.toInt().toString(),
                                            onValueChange = {
                                                it.toDoubleOrNull()?.let { newRadius ->
                                                    radiusMeters = newRadius
                                                }
                                            },
                                            label = { Text("Radius (meters)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true,
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.End,
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            TextButton(onClick = {
                                                showDialog = false
                                                editingLocation = false
                                                formInput = ""
                                                radiusMeters = 100.0
                                            }) {
                                                Text("Cancel")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TextButton(
                                                onClick = {
                                                    val loc =
                                                        Location(
                                                            id = 1,
                                                            name = formInput,
                                                            latitude = selectedPoint!!.latitude,
                                                            longitude = selectedPoint!!.longitude,
                                                            radius = radiusMeters,
                                                        )
                                                    viewModel.addLocation(loc)
                                                    Log.d(
                                                        "MapScreen",
                                                        "Confirmed: ${selectedPoint?.latitude}, " +
                                                            "${selectedPoint?.longitude} " +
                                                            "with radius $radiusMeters " +
                                                            "and note: $formInput",
                                                    )
                                                    showDialog = false
                                                    editingLocation = false
                                                    formInput = ""
                                                    radiusMeters = 100.0
                                                    selectedPoint = null
                                                },
                                            ) {
                                                Text("Add Location")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is MapScreenState.Loading -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    ) {
                        LoadingView()
                    }
                }
                is MapScreenState.Success -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    ) {
                        SuccessView(
                            message = "Location added successfully",
                            onButtonClick = { onAddedLocation() },
                        )
                    }
                }
                is MapScreenState.Error -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    ) {
                        ErrorAlert(
                            title = "Add Location Error",
                            message = mapState.message,
                            buttonText = "Ok",
                            onDismiss = { viewModel.setIdleState() },
                        )
                    }
                }
            }
        }
    }
}

/**
 * An effect that request location updates based on the provided request and ensures that the
 * updates are added and removed whenever the composable enters or exists the composition.
 */
@RequiresPermission(
    anyOf = [Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION],
)
@Composable
fun LocationUpdatesEffect(
    locationRequest: LocationRequest,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onUpdate: (result: LocationResult) -> Unit,
) {
    val context = LocalContext.current
    val currentOnUpdate by rememberUpdatedState(newValue = onUpdate)

    // Whenever on of these parameters changes, dispose and restart the effect.
    DisposableEffect(locationRequest, lifecycleOwner) {
        val locationClient = LocationServices.getFusedLocationProviderClient(context)
        val locationCallback: LocationCallback =
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    currentOnUpdate(result)
                }
            }
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_START) {
                    locationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper(),
                    )
                } else if (event == Lifecycle.Event.ON_STOP) {
                    locationClient.removeLocationUpdates(locationCallback)
                }
            }

        // Add the observer to the lifecycle
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            locationClient.removeLocationUpdates(locationCallback)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
