package com.tasa.ui.screens.editloc

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Location
import com.tasa.domain.UserInfoRepository
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationService
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import com.tasa.ui.screens.newLocation.TasaLocation
import com.tasa.utils.Failure
import com.tasa.utils.ServiceKiller
import com.tasa.utils.StringResourceResolver
import com.tasa.utils.Success
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import kotlin.coroutines.cancellation.CancellationException

sealed interface EditLocScreenState {
    data object Loading : EditLocScreenState

    data object Uninitialized : EditLocScreenState

    data class ChangingFields(
        val location: StateFlow<Location>,
        val selectedPoint: StateFlow<GeoPoint?>,
        val radius: StateFlow<Double>,
        val locationName: StateFlow<String>,
    ) : EditLocScreenState

    data object Success : EditLocScreenState

    data class Error(val error: String) : EditLocScreenState

    data class ChangingCenter(
        val selectedPoint: StateFlow<GeoPoint?>,
        val searchQuery: StateFlow<TextFieldValue>,
        val userActivity: StateFlow<String?>,
        val radius: StateFlow<Double>,
        val locationName: StateFlow<String>,
    ) : EditLocScreenState

    data object SessionExpired : EditLocScreenState
}

class EditLocScreenViewModel(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    private val stringResolver: StringResourceResolver,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    private val serviceKiller: ServiceKiller,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    initialPoint: GeoPoint,
    initialState: EditLocScreenState = EditLocScreenState.Uninitialized,
) : ViewModel() {
    private val _activityState = MutableStateFlow<String?>(null)
    val activityState: StateFlow<String?> = _activityState.asStateFlow()

    private val _state = MutableStateFlow<EditLocScreenState>(initialState)
    val state: StateFlow<EditLocScreenState> = _state.asStateFlow()

    private val _selectedPoint = MutableStateFlow<GeoPoint>(initialPoint)
    val selectedPoint: StateFlow<GeoPoint> = _selectedPoint.asStateFlow()

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

    val currentLocation: StateFlow<TasaLocation> = _currentLocation.asStateFlow()

    fun updateLocation(location: Location) {
        if (_state.value is EditLocScreenState.ChangingCenter) {
            val point = GeoPoint(location.latitude, location.longitude)
            _currentLocation.value = TasaLocation(point, 10f, null, null, 0)
        }
    }

    private val _radius = MutableStateFlow<Double>(30.0)
    val radius: StateFlow<Double> = _radius.asStateFlow()

    private val _locationName = MutableStateFlow<String>("")
    val locationName: StateFlow<String> = _locationName.asStateFlow()

    private val _query =
        MutableStateFlow<TextFieldValue>(
            TextFieldValue(""),
        )
    val query: StateFlow<TextFieldValue> = _query.asStateFlow()

    fun updateRadius(radius: Double) {
        if (_state.value is EditLocScreenState.ChangingCenter) {
            _radius.value = radius
            _state.value =
                (_state.value as EditLocScreenState.ChangingCenter).copy(
                    radius = _radius,
                )
        }
    }

    fun editLocationName(string: String) {
        if (_state.value is EditLocScreenState.ChangingCenter) {
            _locationName.value = string
            _state.value =
                (_state.value as EditLocScreenState.ChangingCenter).copy(
                    locationName = _locationName,
                )
        }
    }

    fun updateSelectedPoint(point: GeoPoint) {
        if (_state.value is EditLocScreenState.ChangingCenter) {
            _selectedPoint.value = point
        }
    }

    fun updateLocCenter(location: Location) {
        if (_state.value is EditLocScreenState.Loading) return
        _state.value = EditLocScreenState.Loading
        viewModelScope.launch {
            try {
                repo.locationRepo.updateLocation(location)
            } catch (e: Exception) {
                _state.value = EditLocScreenState.Error(e.message ?: "Unknown Error")
                Log.e("EditLocViewModel", "Error updating location: ${e.message}")
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onChangeCenter(
        location: Location,
        locationName: String,
        radius: Double,
        latitude: Double,
        longitude: Double,
    ) {
        println("oldLoc: lat:${location.latitude} lon: ${location.longitude}")
        println("newLoc: lat:$latitude lon:$longitude")
        if (_state.value is EditLocScreenState.ChangingCenter) {
            viewModelScope.launch {
                try {
                    if (repo.locationRepo.getLocationByName(locationName) != null &&
                        repo.locationRepo.getLocationByName(locationName)?.id != location.id
                    ) {
                        _state.value =
                            EditLocScreenState.Error(
                                stringResolver.getString(R.string.error_another_location_name_already_exists),
                            )
                        return@launch
                    }
                    when (val result0 = repo.locationRepo.deleteLocationById(location.id)) {
                        is Failure -> {
                            _state.value =
                                EditLocScreenState.Error(result0.value.message)
                            return@launch
                        }
                        is Success -> {
                            when (
                                val result1 =
                                    repo.locationRepo.insertLocation(
                                        name = locationName,
                                        latitude = latitude,
                                        longitude = longitude,
                                        radius = radius,
                                    )
                            ) {
                                is Failure -> {
                                    _state.value = EditLocScreenState.Error(result1.value.message)
                                    return@launch
                                }
                                is Success -> {
                                    val newLocation =
                                        repo.locationRepo.getLocationByName(locationName) ?: location

                                    when (val result2 = repo.ruleRepo.insertRuleLocationTimeless(newLocation)) {
                                        is Success -> {
                                            val radius =
                                                if (newLocation.radius < 100) {
                                                    100f
                                                } else {
                                                    newLocation.radius.toFloat()
                                                }
                                            geofenceManager.registerGeofence(
                                                newLocation.name,
                                                newLocation.toLocation(),
                                                radius,
                                            )
                                            repo.geofenceRepo.createGeofence(
                                                newLocation,
                                                result2.value,
                                            )
                                        }
                                        is Failure -> {
                                            _state.value =
                                                EditLocScreenState.Error(
                                                    result2.value.message,
                                                )
                                            return@launch
                                        }
                                    }
                                    _state.value =
                                        EditLocScreenState.Success
                                }
                            }
                        }
                    }
                } catch (ex: AuthenticationException) {
                    _state.value =
                        EditLocScreenState.SessionExpired
                    return@launch
                } catch (ex: Throwable) {
                    _state.value =
                        EditLocScreenState.Error(
                            stringResolver.getString(R.string.unexpected_error),
                        )
                }
            }
        }
    }

    fun setEditingCenterState() {
        if (state.value is EditLocScreenState.ChangingFields) {
            _state.value =
                EditLocScreenState.ChangingCenter(
                    selectedPoint = _selectedPoint,
                    radius = _radius,
                    locationName = _locationName,
                    searchQuery = _query,
                    userActivity = activityState,
                )
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun onChangeLocationFields(
        location: Location,
        locationName: String,
        radius: Double,
    ) {
        if (_state.value is EditLocScreenState.ChangingFields) {
            viewModelScope.launch {
                try {
                    if (repo.locationRepo.getLocationByName(locationName) != null &&
                        repo.locationRepo.getLocationByName(locationName)?.id != location.id
                    ) {
                        _state.value =
                            EditLocScreenState.Error(
                                stringResolver.getString(R.string.error_another_location_name_already_exists),
                            )
                        return@launch
                    }

                    val latitude = location.latitude
                    val longitude = location.longitude

                    when (val result0 = repo.locationRepo.deleteLocationById(location.id)) {
                        is Failure -> {
                            _state.value =
                                EditLocScreenState.Error(result0.value.message)
                            return@launch
                        }
                        is Success -> {
                            when (
                                val result1 =
                                    repo.locationRepo.insertLocation(
                                        name = locationName,
                                        latitude = latitude,
                                        longitude = longitude,
                                        radius = radius,
                                    )
                            ) {
                                is Failure -> {
                                    _state.value = EditLocScreenState.Error(result1.value.message)
                                    return@launch
                                }
                                is Success -> {
                                    val newLocation =
                                        repo.locationRepo.getLocationByName(locationName) ?: location

                                    when (val result2 = repo.ruleRepo.insertRuleLocationTimeless(newLocation)) {
                                        is Success -> {
                                            val radius =
                                                if (newLocation.radius < 100) {
                                                    100f
                                                } else {
                                                    newLocation.radius.toFloat()
                                                }
                                            geofenceManager.registerGeofence(
                                                newLocation.name,
                                                newLocation.toLocation(),
                                                radius,
                                            )
                                            repo.geofenceRepo.createGeofence(
                                                newLocation,
                                                result2.value,
                                            )
                                        }
                                        is Failure -> {
                                            _state.value =
                                                EditLocScreenState.Error(
                                                    result2.value.message,
                                                )
                                            return@launch
                                        }
                                    }
                                    _state.value =
                                        EditLocScreenState.Success
                                }
                            }
                        }
                    }
                } catch (ex: AuthenticationException) {
                    _state.value = EditLocScreenState.SessionExpired
                    return@launch
                } catch (ex: Throwable) {
                    _state.value =
                        EditLocScreenState.Error(
                            stringResolver.getString(R.string.unexpected_error),
                        )
                }
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun createTimelessRule(location: Location) {
        if (_state.value is EditLocScreenState.ChangingFields) {
            viewModelScope.launch {
                try {
                    val timelessRulesForLocation =
                        repo.ruleRepo.getTimelessRulesForLocation(location)
                    if (timelessRulesForLocation.isNotEmpty()) {
                        _state.value =
                            EditLocScreenState.Error(
                                stringResolver.getString(R.string.rule_already_exists_for_this_location),
                            )
                        return@launch
                    }

                    when (val result = repo.ruleRepo.insertRuleLocationTimeless(location)) {
                        is Success -> {
                            val radius =
                                if (location.radius < 100) {
                                    100f
                                } else {
                                    location.radius.toFloat()
                                }
                            geofenceManager.registerGeofence(
                                location.name,
                                location.toLocation(),
                                radius,
                            )
                            repo.geofenceRepo.createGeofence(
                                location,
                                result.value,
                            )
                        }

                        is Failure -> {
                            _state.value =
                                EditLocScreenState.Error(
                                    result.value.message,
                                )
                            return@launch
                        }
                    }
                    _state.value =
                        EditLocScreenState.Success
                } catch (ex: AuthenticationException) {
                    _state.value = EditLocScreenState.SessionExpired
                    return@launch
                } catch (ex: Throwable) {
                    _state.value =
                        EditLocScreenState.Error(
                            stringResolver.getString(
                                R.string.unexpected_error,
                            ),
                        )
                }
            }
        }
    }

    /*
    fun hasRule(
        location: Location
    ): Boolean {
        if (_state.value is EditLocScreenState.ChangingFields) {
            viewModelScope.launch {
                try {
                    val timelessRulesForLocation =
                        repo.ruleRepo.getTimelessRulesForLocation(location)
                    return@launch timelessRulesForLocation.isNotEmpty()


                } catch (ex: AuthenticationException) {
                    _state.value = EditLocScreenState.SessionExpired
                    return@launch
                } catch (ex: Throwable) {
                    _state.value = EditLocScreenState.Error(
                        stringResolver.getString(
                            R.string.unexpected_error
                        )
                    )
                }
            }
        }
    }

     */

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun deleteTimelessRule(location: Location) {
        if (_state.value is EditLocScreenState.ChangingFields) {
            viewModelScope.launch {
                try {
                    val timelessRulesForLocation =
                        repo.ruleRepo.getTimelessRulesForLocation(location)
                    if (timelessRulesForLocation.isEmpty()) {
                        _state.value =
                            EditLocScreenState.Error(
                                stringResolver.getString(R.string.no_rule_for_this_location),
                            )
                        return@launch
                    }

                    when (val result = repo.ruleRepo.deleteRuleLocationTimeless(timelessRulesForLocation[0])) {
                        is Success -> {
                            _state.value =
                                EditLocScreenState.Success
                        }

                        is Failure -> {
                            _state.value =
                                EditLocScreenState.Error(
                                    result.value.message,
                                )
                            return@launch
                        }
                    }
                } catch (ex: AuthenticationException) {
                    _state.value = EditLocScreenState.SessionExpired
                    return@launch
                } catch (ex: Throwable) {
                    _state.value =
                        EditLocScreenState.Error(
                            stringResolver.getString(
                                R.string.unexpected_error,
                            ),
                        )
                }
            }
        }
    }

    fun onFatalError(): Job? {
        if (_state.value !is EditLocScreenState.SessionExpired) return null
        return viewModelScope.launch {
            try {
                val job = clearOnFatalError()
                job.join()
                userInfo.clearUserInfo()
            } catch (e: Throwable) {
            }
        }
    }

    fun clearOnFatalError() =
        viewModelScope.launch {
            try {
                locationUpdatesRepository.forceStop()
                if (LocationService.isRunning) {
                    serviceKiller.killServices(LocationService::class)
                }
                val alarms = repo.alarmRepo.getAllAlarms()
                alarms.forEach { alarm ->
                    alarmScheduler.cancelAlarm(alarm.id, alarm.action)
                    repo.alarmRepo.deleteAlarm(alarm.id)
                }
                val geofences = repo.geofenceRepo.getAllGeofences()
                geofences.forEach { geofence ->
                    geofenceManager.deregisterGeofence(geofence.name)
                    repo.geofenceRepo.deleteGeofence(geofence)
                }
                repo.ruleRepo.clean()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                repo.userRepo.clear()
            } catch (e: CancellationException) {
            } catch (e: Throwable) {
                userInfo.clearUserInfo()
            }
        }

    fun initializeEditing(location: Location) {
        val locState = MutableStateFlow(location)
        val selectedPoint = MutableStateFlow<GeoPoint?>(GeoPoint(location.latitude, location.longitude))
        val radiusState = MutableStateFlow(location.radius)
        val nameState = MutableStateFlow(location.name)

        _state.value =
            EditLocScreenState.ChangingFields(
                location = locState,
                selectedPoint = selectedPoint,
                radius = radiusState,
                locationName = nameState,
            )
    }
}

@Suppress("UNCHECKED_CAST")
class EditLocScreenViewModelFactory(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    private val stringResolver: StringResourceResolver,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    private val serviceKiller: ServiceKiller,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val initialPoint: GeoPoint,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditLocScreenViewModel(
            repo,
            userInfo,
            stringResolver,
            locationUpdatesRepository,
            serviceKiller,
            alarmScheduler,
            geofenceManager,
            initialPoint,
        ) as T
    }
}
