@file:Suppress("ktlint")

package com.tasa.ui.screens.newLocation

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import androidx.annotation.RequiresPermission
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tasa.activity.UserActivityTransitionManager
import com.tasa.domain.Location
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toLocalDateTime
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

data class TasaLocation(
    val point: GeoPoint,
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val time: LocalDateTime? = null,
    val updates: Int? = null,
)

fun android.location.Location.toTasaLocation(): TasaLocation {
    return TasaLocation(
        point = GeoPoint(latitude, longitude),
        accuracy = accuracy,
        altitude = altitude,
        time = null,
        updates = 0,
    )
}

sealed interface MapsScreenState {
    data object Uninitialized : MapsScreenState

    data object Loading : MapsScreenState

    data class Success(
        val selectedPoint: StateFlow<GeoPoint?>,
        val currentLocation: StateFlow<TasaLocation>,
        val searchQuery: StateFlow<TextFieldValue>,
        val userActivity: StateFlow<String?>,
        val radius: StateFlow<Double>,
        val locationName: StateFlow<String>,
    ) : MapsScreenState

    data class SuccessSearching(
        val selectedPoint: StateFlow<GeoPoint?>,
        val currentLocation: StateFlow<TasaLocation>,
        val searchQuery: StateFlow<TextFieldValue>,
        val userActivity: StateFlow<String?>,
        val radius: StateFlow<Double>,
        val locationName: StateFlow<String>,
    ) : MapsScreenState

    data class EditingLocation(
        val selectedPoint: StateFlow<GeoPoint?>,
        val currentLocation: StateFlow<TasaLocation>,
        val searchQuery: StateFlow<TextFieldValue>,
        val userActivity: StateFlow<String?>,
        val radius: StateFlow<Double>,
        val locationName: StateFlow<String>,
    ) : MapsScreenState

    data class Error(val message: String) : MapsScreenState
}

class MapScreenViewModel(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    private val locationClient: FusedLocationProviderClient,
    private val activityRecognitionManager: UserActivityTransitionManager,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    initialState: MapsScreenState = MapsScreenState.Uninitialized,
) : ViewModel() {
    private val _activityState = MutableStateFlow<String?>(null)
    val activityState: StateFlow<String?> = _activityState.asStateFlow()

    private val _state = MutableStateFlow<MapsScreenState>(initialState)
    val state: StateFlow<MapsScreenState> = _state.asStateFlow()

    private val _selectedPoint = MutableStateFlow<GeoPoint?>(null)
    val selectedPoint: StateFlow<GeoPoint?> = _selectedPoint.asStateFlow()

    private val _gettingUpdates = MutableStateFlow<Boolean>(false)
    val gettingUpdates: StateFlow<Boolean> = _gettingUpdates.asStateFlow()

    private val _mapIsReady = MutableStateFlow<Boolean>(false)

    private val _locationReady = MutableStateFlow<Boolean>(false)

    private val _currentLocation =
        MutableStateFlow<TasaLocation>(
            TasaLocation(
                point = GeoPoint(0.0, 0.0),
                accuracy = null,
                altitude = null,
                time = null,
                updates = 0,
            ),
        )

    private val _radius = MutableStateFlow<Double>(30.0)
    val radius: StateFlow<Double> = _radius.asStateFlow()

    private val _locationName = MutableStateFlow<String>("")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _query =
        MutableStateFlow<TextFieldValue>(
            TextFieldValue(""),
        )
    val query: StateFlow<TextFieldValue> = _query.asStateFlow()

    val currentLocation: StateFlow<TasaLocation?> = _currentLocation.asStateFlow()

    var locationCallback: LocationCallback? = null

    fun getLocationFromSearchQuery(context: Context) {
        if ((_state.value is MapsScreenState.SuccessSearching || _state.value is MapsScreenState.Success) &&
            _query.value.text.isNotEmpty() && _query.value.text.isNotBlank()
        ) {
            viewModelScope.launch {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocationName(_query.value.text, 1)
                    if (addresses != null && addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val point = GeoPoint(address.latitude, address.longitude)
                        _selectedPoint.value = point
                        _state.value =
                            MapsScreenState.Success(
                                selectedPoint = _selectedPoint,
                                currentLocation = _currentLocation,
                                searchQuery = _query,
                                userActivity = activityState,
                                radius = _radius,
                                locationName = _locationName,
                            )
                    } else {
                        _state.value = MapsScreenState.Error("No results found for '$query'")
                    }
                } catch (e: Exception) {
                    _state.value = MapsScreenState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun notifyMapReady() {
        _mapIsReady.value = true
        tryToSetReadyState()
    }

    private fun tryToSetReadyState() {
        if (_mapIsReady.value && _locationReady.value) {
            /* _state.value =
                 MapsScreenState.Success(
                     selectedPoint = _selectedPoint,
                     currentLocation = _currentLocation,
                     userActivity = currentUserActivity,
                     searchQuery = _query,
                     radius = _radius,
                     locationName = _locationName,
                 )*/
        }
    }

    fun setEditingLocationState(
        radius: Double = 30.0,
        locationName: String = "",
    ) {
        if (state.value is MapsScreenState.Success || state.value is MapsScreenState.SuccessSearching) {
            _state.value =
                MapsScreenState.EditingLocation(
                    selectedPoint = _selectedPoint,
                    currentLocation = _currentLocation,
                    radius = _radius,
                    locationName = _locationName,
                    searchQuery = _query,
                    userActivity = activityState,
                )
        }
    }

    fun editLocationName(string: String) {
        if (_state.value is MapsScreenState.EditingLocation) {
            _locationName.value = string
            _state.value =
                (_state.value as MapsScreenState.EditingLocation).copy(
                    locationName = _locationName,
                )
        }
    }

    fun setSearchingState() {
        if (state.value is MapsScreenState.Success || state.value is MapsScreenState.EditingLocation) {
            _state.value =
                MapsScreenState.SuccessSearching(
                    selectedPoint = _selectedPoint,
                    currentLocation = _currentLocation,
                    radius = _radius,
                    locationName = _locationName,
                    searchQuery = _query,
                    userActivity = activityState,
                )
        }
    }

    fun setUnSearchingState() {
        if (_state.value is MapsScreenState.SuccessSearching) {
            _state.value =
                MapsScreenState.Success(
                    selectedPoint = _selectedPoint,
                    currentLocation = _currentLocation,
                    radius = _radius,
                    locationName = _locationName,
                    searchQuery = _query,
                    userActivity = activityState,
                )
        }
    }

    fun updateRadius(radius: Double) {
        if (_state.value is MapsScreenState.EditingLocation) {
            _radius.value = radius
            _state.value =
                (_state.value as MapsScreenState.EditingLocation).copy(
                    radius = _radius,
                )
        }
    }

    fun updateSelectedPoint(point: GeoPoint) {
        if (_state.value is MapsScreenState.Success) {
            _selectedPoint.value = point
            /*_state.value =
                (_state.value as MapsScreenState.Success).copy(
                    selectedPoint = _selectedPoint,
                )
        } else {
            _selectedPoint.value = point
            _state.value = MapsScreenState.Success(
                selectedPoint = _selectedPoint,
                currentLocation = _currentLocation,
            )*/
        }
    }

    fun updateSearchQuery(query: TextFieldValue) {
        val b = _state.value
        // if (_state.value is MapsScreenState.SuccessSearching) {
        val a = query
        _query.value = query
        _state.value =
            MapsScreenState.SuccessSearching(
                selectedPoint = _selectedPoint,
                currentLocation = _currentLocation,
                searchQuery = _query,
                userActivity = activityState,
                radius = _radius,
                locationName = _locationName,
            )
        //   }
    }

    fun onDismissEditingLocation() {
        if (_state.value is MapsScreenState.EditingLocation) {
            _state.value =
                MapsScreenState.Success(
                    selectedPoint = _selectedPoint,
                    currentLocation = _currentLocation,
                    searchQuery = _query,
                    userActivity = activityState,
                    radius = _radius,
                    locationName = _locationName,
                )
        }
    }

    fun onCreateLocation(
        locationName: String,
        radius: Double,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit = {},
    ) {
        if (_state.value is MapsScreenState.EditingLocation) {
            viewModelScope.launch {
                try {
                    repo.locationRepo.insertLocation(
                        Location(
                            id = null,
                            name = locationName,
                            latitude = latitude,
                            longitude = longitude,
                            radius = radius,
                        ),
                    )
                    onSuccess()
                } catch (ex: Throwable) {
                    _state.value = MapsScreenState.Error(ex.message ?: "Unknown error")
                }
            }
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACTIVITY_RECOGNITION,
            "com.google.android.gms.permission.ACTIVITY_RECOGNITION",
        ],
    )
    fun getActivityState(): Job? {
        return viewModelScope.launch {
            try {
                val result = activityRecognitionManager.registerActivityTransitions()
                if (result.isFailure) {
                    _state.value =
                        MapsScreenState.Error(
                            result.exceptionOrNull()?.message ?: "Failed to register activity transitions",
                        )
                    return@launch
                } else {
                    userInfo.lastActivity
                        .collectLatest {
                            if (it == null) {
                                _activityState.value = null
                            } else {
                                _activityState.value = UserActivityTransitionManager.Companion.getActivityType(it)
                            }
                        }
                }
            } catch (ex: Throwable) {
                _state.value = MapsScreenState.Error(ex.message ?: "Unknown error")
            }
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ],
    )
    fun keepGivenCurrentLocation(): Job? {
        if (_state.value == MapsScreenState.Loading) return null
        _state.value = MapsScreenState.Loading
        return viewModelScope.launch {
            try {
                /* // first get the current location
                 _currentLocation.value = getCurrentLocation()
                 _state.value =
                     MapsScreenState.Success(
                         selectedPoint = _selectedPoint,
                         currentLocation = _currentLocation,
                         searchQuery = _query,
                         userActivity = activityState,
                         radius = _radius,
                         locationName = _locationName,
                     )
                 _locationReady.value = true
                 tryToSetReadyState()
                 var locationRequest =
                     LocationRequest.Builder(
                         Priority.PRIORITY_HIGH_ACCURACY,
                         3.seconds.inWholeMilliseconds,
                     )
                         .setWaitForAccurateLocation(true)
                         .build()
                 // watch for user movements
                 _activityState.collect {
                     // if user moves then watch for updates
                     when (it) {
                         "ON_FOOT", "WALKING" -> {
                             if (locationCallback == null && _gettingUpdates.value == false) {
                                 startLocationUpdates(
                                     locationRequest = locationRequest,
                                 )
                             }
                         }
                         "RUNNING" -> {
                             locationRequest =
                                 LocationRequest.Builder(
                                     Priority.PRIORITY_HIGH_ACCURACY, 2.seconds.inWholeMilliseconds,
                                 )
                                     .setWaitForAccurateLocation(true)
                                     .build()
                             if (locationCallback != null && _gettingUpdates.value == false) {
                                 // if user is running then update more frequently
                                 stopLocationUpdates()
                             }
                             if (locationCallback == null && _gettingUpdates.value == false) {
                                 startLocationUpdates(
                                     locationRequest = locationRequest,
                                 )
                             }
                         }
                         "STILL" -> {
                             if (locationCallback != null && _gettingUpdates.value == false) {
                                 // if user is still then stop updates
                                 stopLocationUpdates()
                             }
                         }
                     }
                 }*/
                var locationRequest =
                    LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        3.seconds.inWholeMilliseconds,
                    )
                        .setWaitForAccurateLocation(true)
                        .build()
                getCurrentLocation().let { location ->
                    _currentLocation.value = location
                    _state.value =
                        MapsScreenState.Success(
                            selectedPoint = _selectedPoint,
                            currentLocation = _currentLocation,
                            searchQuery = _query,
                            userActivity = activityState,
                            radius = _radius,
                            locationName = _locationName,
                        )
                }
                locationUpdatesRepository.startUp()
                locationUpdatesRepository.centralLocationFlow.collect { location ->
                    _currentLocation.value = location
                    // WRONG
                    if ((_state.value is MapsScreenState.Loading || _state.value is MapsScreenState.Success)) {
                        _state.value =
                            MapsScreenState.Success(
                                selectedPoint = _selectedPoint,
                                currentLocation = _currentLocation,
                                searchQuery = _query,
                                userActivity = activityState,
                                radius = _radius,
                                locationName = _locationName,
                            )
                    }
                }
            } catch (ex: Throwable) {
                _state.value = MapsScreenState.Error(ex.message ?: "Unknown error")
            }
        }
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ],
    )
    fun restartLocationUpdates() {
        if (_state.value == MapsScreenState.Loading) return
        _gettingUpdates.value = true
        viewModelScope.launch {
            try {
                var locationRequest =
                    LocationRequest.Builder(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        3.seconds.inWholeMilliseconds,
                    )
                        .setWaitForAccurateLocation(true)
                        .build()
                startLocationUpdates(locationRequest)
            } catch (ex: Throwable) {
                _state.value = MapsScreenState.Error(ex.message ?: "Unknown error")
            }
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ],
    )
    fun stopLocationUpdates() {
        _gettingUpdates.value = false
        locationCallback?.let {
            locationClient.removeLocationUpdates(it)
            locationCallback = null
        }
        _state.value =
            MapsScreenState.Success(
                selectedPoint = _selectedPoint,
                currentLocation = _currentLocation,
                locationName = _locationName,
                searchQuery = _query,
                userActivity = activityState,
                radius = _radius,
            )
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ],
    )
    private fun startLocationUpdates(locationRequest: LocationRequest) {
        locationCallback =
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    for (location in result.locations) {
                        // check for accuracy TODO
                        _currentLocation.value =
                            TasaLocation(
                                point = GeoPoint(location.latitude, location.longitude),
                                accuracy = location.accuracy,
                                altitude = location.altitude,
                                time = location.time.toLocalDateTime(),
                                updates = _currentLocation.value.updates?.plus(1) ?: 1,
                            )
                    }
                }
            }
        locationCallback?.let {
            locationClient.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper(),
            )
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocation(): TasaLocation {
        val priority = Priority.PRIORITY_HIGH_ACCURACY
        val result =
            locationClient.getCurrentLocation(
                priority,
                CancellationTokenSource().token,
            ).await()
        return result?.let { fetchedLocation ->
            TasaLocation(
                point = GeoPoint(fetchedLocation.latitude, fetchedLocation.longitude),
                accuracy = fetchedLocation.accuracy,
                altitude = fetchedLocation.altitude,
                time = fetchedLocation.time.toLocalDateTime(),
                updates = 1,
            )
        } ?: throw Exception("Localização não encontrada")
    }
}

@Suppress("UNCHECKED_CAST")
class MapScreenViewModelFactory(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    private val locationClient: FusedLocationProviderClient,
    private val activityRecognitionManager: UserActivityTransitionManager,
    private val locationUpdatesRepository: LocationUpdatesRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapScreenViewModel(
            repo = repo,
            userInfo = userInfo,
            locationClient = locationClient,
            activityRecognitionManager = activityRecognitionManager,
            locationUpdatesRepository = locationUpdatesRepository,
            initialState = MapsScreenState.Uninitialized,
        ) as T
    }
}
