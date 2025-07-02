@file:Suppress("ktlint")

package com.tasa.ui.screens.newLocation

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.annotation.RequiresPermission
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tasa.R
import com.tasa.activity.UserActivityTransitionManager
import com.tasa.domain.Location
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toLocalDateTime
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import com.tasa.utils.Failure
import com.tasa.utils.Success
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
import android.location.Location as AndroidLocation

data class TasaLocation(
    val point: GeoPoint,
    val accuracy: Float,
    val altitude: Double? = null,
    val time: LocalDateTime? = null,
    val updates: Int? = null,
) {
    fun toLocation(): AndroidLocation {
        return AndroidLocation("TasaLocation").apply {
            latitude = point.latitude
            longitude = point.longitude
            accuracy = accuracy
        }
    }
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

    data class Error(val error: String) : MapsScreenState
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


    private val _currentLocation =
        MutableStateFlow<TasaLocation>(
            TasaLocation(
                point = GeoPoint(0.0, 0.0),
                accuracy = 10f,
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

    val currentLocation: StateFlow<TasaLocation> = _currentLocation.asStateFlow()


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
                        _state.value = MapsScreenState.Error(R.string.no_results_found.toString())
                    }
                } catch (e: Exception) {
                    _state.value = MapsScreenState.Error(R.string.unexpected_error.toString())
                }
            }
        }
    }



    fun setEditingLocationState() {
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
        }
    }

    fun updateSearchQuery(query: TextFieldValue) {
        // if (_state.value is MapsScreenState.SuccessSearching) {
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
                    if (repo.locationRepo.getLocationByName(locationName) != null) {
                        _state.value = MapsScreenState.Error(R.string.error_location_name_already_exists.toString())
                        return@launch
                    }
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
                    _state.value = MapsScreenState.Error(R.string.unexpected_error.toString())
                }
            }
        }
    }

    // TODO remove
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
                    /*_state.value =
                        MapsScreenState.Error(
                            result.exceptionOrNull()?.message ?: "Failed to register activity transitions",
                        )*/
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
    fun keepGivenCurrentLocation(context: Context): Job? {
        if (_state.value == MapsScreenState.Loading) return null
        _state.value = MapsScreenState.Loading
        return viewModelScope.launch {
            try {
                val locationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                if (!isGpsEnabled) {
                    _state.value = MapsScreenState.Error(R.string.location_disabled_warning.toString())
                    return@launch
                }
                getCurrentLocation().let { location ->
                    _currentLocation.value = location
                    _selectedPoint.value = location.point
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
                    if (location != null){
                        _currentLocation.value = location
                    }
                }
            } catch (ex: Throwable) {
                _state.value = MapsScreenState.Error(R.string.unexpected_error.toString())
            }
        }
    }


    fun stopLocationUpdates() {
        locationUpdatesRepository.stop()
    }

    fun recenterMap() {
        if (_state.value is MapsScreenState.Loading) return
        _selectedPoint.value = _currentLocation.value.point
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
