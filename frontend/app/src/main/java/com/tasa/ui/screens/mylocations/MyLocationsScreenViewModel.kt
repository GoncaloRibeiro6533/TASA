package com.tasa.ui.screens.mylocations

import android.Manifest
import androidx.annotation.RequiresPermission
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
import com.tasa.utils.Failure
import com.tasa.utils.ServiceKiller
import com.tasa.utils.StringResourceResolver
import com.tasa.utils.Success
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import kotlin.coroutines.cancellation.CancellationException

sealed interface MyLocationsScreenState {
    data object Uninitialized : MyLocationsScreenState

    data object Loading : MyLocationsScreenState

    data class Error(val message: String, val sessionExpired: Boolean = false) : MyLocationsScreenState

    data class Success(
        val locations: StateFlow<List<Location>>,
        val messageOfSuccess: Int? = null,
    ) : MyLocationsScreenState

    data class CreatingRuleLocation(
        val location: Location,
        val startTime: LocalDateTime? = null,
        val endTime: LocalDateTime? = null,
    ) : MyLocationsScreenState

    data object SessionExpired : MyLocationsScreenState
}

class MyLocationsScreenViewModel(
    private val repo: TasaRepo,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    private val stringResolver: StringResourceResolver,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    initialState: MyLocationsScreenState = MyLocationsScreenState.Uninitialized,
) : ViewModel() {
    private val _state: MutableStateFlow<MyLocationsScreenState> = MutableStateFlow(initialState)
    val state: StateFlow<MyLocationsScreenState> = _state.asStateFlow()

    private val _locations: MutableStateFlow<List<Location>> = MutableStateFlow(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

    private val _successMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    val successMessage: StateFlow<Int?> = _successMessage.asStateFlow()

    fun loadLocations(): Job? {
        if (_state.value is MyLocationsScreenState.Loading) return null
        _state.value = MyLocationsScreenState.Loading
        return viewModelScope.launch {
            try {
                when (val result = repo.locationRepo.fetchLocations()) {
                    is Failure -> {
                        _state.value = MyLocationsScreenState.Error(result.value.message)
                        return@launch
                    }
                    is Success -> {
                        result.value.collect {
                                stream ->
                            _locations.value = stream
                            _state.value = MyLocationsScreenState.Success(_locations)
                        }
                    }
                }
            } catch (e: Throwable) {
                _state.value = MyLocationsScreenState.Error(stringResolver.getString(R.string.unexpected_error))
            }
        }
    }

    fun setCreatingRuleLocationState(location: Location) {
        _state.value = MyLocationsScreenState.CreatingRuleLocation(location)
    }

    fun deleteLocation(location: Location) {
        if (_state.value !is MyLocationsScreenState.Success) return
        _state.value = MyLocationsScreenState.Loading
        viewModelScope.launch {
            try {
                val rule =
                    repo.ruleRepo.getTimelessRulesForLocation(location)
                val geofences =
                    repo.geofenceRepo.getAllGeofences()
                        .filter { it.name == location.name }
                if (rule.isNotEmpty()) {
                    rule.forEach {
                        when (val result = repo.ruleRepo.deleteRuleLocationTimeless(it)) {
                            is Failure -> {
                                _state.value = MyLocationsScreenState.Error(result.value.message)
                                return@launch
                            }
                            is Success -> {
                                geofences.forEach { geofence ->
                                    geofenceManager.deregisterGeofence(geofence.name)
                                }
                                repo.geofenceRepo.deleteGeofence(geofences.first())
                                if (LocationService.isRunning &&
                                    LocationService.locationName == location.name
                                ) {
                                    serviceKiller.killServices(LocationService::class)
                                }
                            }
                        }
                    }
                }
                when (val result = repo.locationRepo.deleteLocation(location)) {
                    is Success -> {
                        _state.value = MyLocationsScreenState.Success(_locations)
                        _successMessage.value = R.string.location_deleted
                    }
                    is Failure -> {
                        _state.value = MyLocationsScreenState.Error(result.value.message)
                    }
                }
            } catch (e: AuthenticationException) {
                _state.value = MyLocationsScreenState.SessionExpired
                return@launch
            } catch (e: Throwable) {
                _state.value = MyLocationsScreenState.Error(stringResolver.getString(R.string.unexpected_error))
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun createTimelessRuleLocation(location: Location) {
        if (_state.value !is MyLocationsScreenState.Success) return
        _state.value = MyLocationsScreenState.Loading
        viewModelScope.launch {
            try {
                val timelessRulesForLocation =
                    repo.ruleRepo.getTimelessRulesForLocation(location)
                if (timelessRulesForLocation.isNotEmpty()) {
                    _state.value =
                        MyLocationsScreenState.Error(
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
                            MyLocationsScreenState.Error(
                                result.value.message,
                            )
                        return@launch
                    }
                }
                _state.value = MyLocationsScreenState.Success(_locations)
                _successMessage.value = R.string.rule_created_successfully
            } catch (e: AuthenticationException) {
                _state.value = MyLocationsScreenState.SessionExpired
                return@launch
            } catch (e: Throwable) {
                _state.value =
                    MyLocationsScreenState.Error(
                        stringResolver.getString(
                            R.string.unexpected_error,
                        ),
                    )
            }
        }
    }

    fun clearMessageOfSuccess() {
        _successMessage.value = null
    }

    fun onFatalError(): Job? {
        if (_state.value !is MyLocationsScreenState.SessionExpired) return null
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
                    alarmScheduler.cancelAlarm(alarm.id, alarm.action)
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
class MyLocationsScreenViewModelFactory(
    private val repo: TasaRepo,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val stringResolver: StringResourceResolver,
    private val locationUpdatesRepository: LocationUpdatesRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyLocationsScreenViewModel(
            repo = repo,
            geofenceManager = geofenceManager,
            serviceKiller = serviceKiller,
            stringResolver = stringResolver,
            userInfo = userInfo,
            alarmScheduler = alarmScheduler,
            locationUpdatesRepository = locationUpdatesRepository,
        ) as T
    }
}
