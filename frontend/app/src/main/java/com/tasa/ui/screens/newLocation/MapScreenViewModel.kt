@file:Suppress("ktlint")

package com.tasa.ui.screens.newLocation

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.AuthenticationException
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toLocalDateTime
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationService
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import com.tasa.utils.Failure
import com.tasa.utils.SearchPlaceService
import com.tasa.utils.ServiceKiller
import com.tasa.utils.StringResourceResolver
import com.tasa.utils.Success
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.osmdroid.util.GeoPoint
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException
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
    data object SessionExpired: MapsScreenState

    data object SuccessCreatingLocation : MapsScreenState
}

class MapScreenViewModel(
    private val repo: TasaRepo,
    private val locationClient: FusedLocationProviderClient,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    private val searchPlaceService: SearchPlaceService,
    private val stringResolver: StringResourceResolver,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
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


    fun getLocationFromSearchQuery() {
        if ((_state.value is MapsScreenState.SuccessSearching || _state.value is MapsScreenState.Success) &&
            _query.value.text.isNotEmpty() && _query.value.text.isNotBlank()
        ) {
            viewModelScope.launch {
                try {
                    val results =
                        searchPlaceService.searchPlace(
                            query = _query.value.text,
                            1
                        )
                    if (results != null) {
                        _selectedPoint.value = GeoPoint(
                            results.latitude,
                            results.longitude,
                        )
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
                        _state.value = MapsScreenState.Error(stringResolver.getString(
                            R.string.no_results_found))
                    }
                } catch (e: Throwable) {
                    _state.value = MapsScreenState.Error(stringResolver.getString(
                        R.string.unexpected_error))
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
        if (_state.value is MapsScreenState.Success || _state.value is MapsScreenState.SuccessSearching) {
        _query.value = query
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

    fun editLocationName(string: String) {
        if (_state.value is MapsScreenState.EditingLocation) {
            _locationName.value = string
            _state.value =
                (_state.value as MapsScreenState.EditingLocation).copy(
                    locationName = _locationName,
                )
        }
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
    ) {
        if (_state.value is MapsScreenState.EditingLocation) {
            viewModelScope.launch {
                try {
                    if (repo.locationRepo.getLocationByName(locationName) != null) {
                        _state.value = MapsScreenState.Error(
                            stringResolver.getString(R.string.error_location_name_already_exists))
                        return@launch
                    }
                    when(val result = repo.locationRepo.insertLocation(
                            name = locationName,
                            latitude = latitude,
                            longitude = longitude,
                            radius = radius,
                        )
                    ){
                        is Failure -> {
                            _state.value = MapsScreenState.Error(result.value.message)
                            return@launch
                        }
                        is Success -> {
                            _state.value =
                                MapsScreenState.SuccessCreatingLocation
                        }
                    }
                }
                catch (ex: AuthenticationException){
                    _state.value = MapsScreenState.SessionExpired
                    return@launch
                }
                catch (ex: Throwable) {
                    _state.value = MapsScreenState.Error(
                        stringResolver.getString(R.string.unexpected_error))
                }
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
                getCurrentLocation().let { location ->
                    if (location == null) {
                        _state.value = 
                            MapsScreenState.Error(stringResolver.getString(R.string.location_disabled_warning))
                        return@launch
                    }
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
                _state.value = MapsScreenState.Error(stringResolver.getString(R.string.unexpected_error))
            }
        }
    }


    fun stopLocationUpdates() {
        if (locationUpdatesRepository.isActive) locationUpdatesRepository.stop()
    }

    fun recenterMap() {
        if (_state.value is MapsScreenState.Loading) return
        _selectedPoint.value = _currentLocation.value.point
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private suspend fun getCurrentLocation(): TasaLocation? {
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
        }
    }

    fun onFatalError(): Job? {
        if (_state.value !is MapsScreenState.SessionExpired) return null
        return clearOnFatalError()
    }

    fun clearOnFatalError() =
        viewModelScope.launch {
            try {
                userInfo.clearUserInfo()
                locationUpdatesRepository.forceStop()
                if (LocationService.isRunning) {
                    serviceKiller.killServices(LocationService::class)
                }
                val alarms = repo.alarmRepo.getAllAlarms()
                alarms.forEach { alarm ->
                    alarmScheduler.cancelAlarm(alarm.id)
                    repo.alarmRepo.deleteAlarm(alarm.id)
                }
                val geofences = repo.geofenceRepo.getAllGeofences()
                geofences.forEach { geofence ->
                    geofenceManager.deregisterGeofence(geofence.name)
                    repo.geofenceRepo.deleteGeofence(geofence)
                }
                repo.userRepo.clear()
                repo.ruleRepo.clean()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
            } catch (e: CancellationException) {
            } catch (e: Throwable) {
                userInfo.clearUserInfo()
            }
        }
}

@Suppress("UNCHECKED_CAST")
class MapScreenViewModelFactory(
    private val repo: TasaRepo,
    private val locationClient: FusedLocationProviderClient,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    private val searchPlaceService: SearchPlaceService,
    private val stringResolver: StringResourceResolver,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapScreenViewModel(
            repo = repo,
            locationClient = locationClient,
            locationUpdatesRepository = locationUpdatesRepository,
            searchPlaceService = searchPlaceService,
            stringResolver = stringResolver,
            userInfo = userInfo,
            alarmScheduler = alarmScheduler,
            geofenceManager = geofenceManager,
            serviceKiller = serviceKiller,
            initialState = MapsScreenState.Uninitialized,
        ) as T
    }
}
