package com.tasa.ui.screens.homepage

import android.Manifest
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toTriggerTime
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
import kotlin.coroutines.cancellation.CancellationException

sealed interface HomeScreenState {
    data object Uninitialized : HomeScreenState

    data object Loading : HomeScreenState

    data class Success(val rules: StateFlow<List<Rule>>) : HomeScreenState

    data class FatalError(val message: String) : HomeScreenState

    data class Error(val message: String) : HomeScreenState

    data object SessionExpired : HomeScreenState
}

class HomePageScreenViewModel(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    private val stringResolver: StringResourceResolver,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    initialState: HomeScreenState = HomeScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeScreenState>(initialState)
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    private val _isLocal = MutableStateFlow<Boolean>(false)
    val isLocal: StateFlow<Boolean> = _isLocal.asStateFlow()

    fun onFatalError(): Job? {
        if (_state.value !is HomeScreenState.SessionExpired ||
            _state.value !is HomeScreenState.FatalError
        ) {
            return null
        }
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

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ],
    )
    fun registerGeofences() {
        viewModelScope.launch {
            try {
                val list = repo.geofenceRepo.getAllGeofences()
                if (list.isNotEmpty()) {
                    geofenceManager.onBootRegisterGeofences(list)
                }
            } catch (e: Throwable) {
            }
        }
    }

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ],
    )
    fun loadLocalData(): Job? {
        if (_state.value is HomeScreenState.Loading) return null
        _state.value = HomeScreenState.Loading
        return viewModelScope.launch {
            try {
                when (val result = repo.ruleRepo.fetchAllRules()) {
                    is Success ->
                        result.value.collect { stream ->
                            _rules.value = stream
                            _state.value = HomeScreenState.Success(rules)
                        }
                    is Failure -> {
                        _state.value = HomeScreenState.Error(result.value.message)
                    }
                }
            } catch (e: CancellationException) {
                return@launch
            } catch (e: AuthenticationException) {
                _state.value = HomeScreenState.SessionExpired
                return@launch
            } catch (e: Throwable) {
                _state.value =
                    HomeScreenState.FatalError(stringResolver.getString(R.string.unexpected_error))
            }
        }
    }

    fun isLocal() {
        viewModelScope.launch {
            try {
                _isLocal.value = userInfo.isLocal()
                if (!locationUpdatesRepository.isActive) {
                    locationUpdatesRepository.forceStop()
                }
            } catch (e: Throwable) {
            }
        }
    }

    fun cancelRule(rule: Rule) {
        viewModelScope.launch {
            try {
                when (rule) {
                    is RuleEvent -> {
                        when (
                            val result =
                                repo.ruleRepo.deleteRuleEvent(
                                    rule,
                                )
                        ) {
                            is Failure -> {
                                _state.value =
                                    HomeScreenState.Error(
                                        result.value.message,
                                    )
                                return@launch
                            }
                            is Success -> {
                                val alarmStart =
                                    repo.alarmRepo
                                        .getAlarmByTriggerTime(rule.startTime.toTriggerTime().value)
                                val alarmEnd =
                                    repo.alarmRepo
                                        .getAlarmByTriggerTime(rule.endTime.toTriggerTime().value)
                                if (alarmStart != null) {
                                    alarmScheduler.cancelAlarm(alarmStart.id)
                                    repo.alarmRepo.deleteAlarm(alarmStart.id)
                                }
                                if (alarmEnd != null) {
                                    alarmScheduler.cancelAlarm(alarmEnd.id)
                                    repo.alarmRepo.deleteAlarm(alarmEnd.id)
                                }
                            }
                        }
                    }
                    is RuleLocation -> {}
                    is RuleLocationTimeless -> {
                        when (val result = repo.ruleRepo.deleteRuleLocationTimeless(rule)) {
                            is Failure -> {
                                _state.value =
                                    HomeScreenState.Error(
                                        result.value.message,
                                    )
                                return@launch
                            }
                            is Success -> {
                                val geofences =
                                    repo.geofenceRepo.getAllGeofences()
                                        .filter { it.name == rule.location.name }
                                if (geofences.isNotEmpty()) {
                                    geofences.forEach { geofence ->
                                        geofenceManager.deregisterGeofence(geofence.name)
                                    }
                                }
                                repo.geofenceRepo.deleteGeofence(geofences.first())
                                if (LocationService.isRunning && LocationService.locationName ==
                                    rule.location.name
                                ) {
                                    serviceKiller.killServices(LocationService::class)
                                }
                            }
                        }
                    }
                }
                _state.value = HomeScreenState.Success(rules)
            } catch (e: AuthenticationException) {
                _state.value = HomeScreenState.SessionExpired
                return@launch
            } catch (e: Throwable) {
                _state.value =
                    HomeScreenState.FatalError(
                        stringResolver.getString(R.string.error_on_cancel_rule),
                    )
            }
        }
    }

    fun clearError() {
        if (_state.value is HomeScreenState.Error) {
            _state.value = HomeScreenState.Success(_rules)
        }
    }
}

@Suppress("UNCHECKED_CAST")
class HomeViewModelFactory(
    private val userInfo: UserInfoRepository,
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    private val stringResolver: StringResourceResolver,
    private val locationUpdatesRepository: LocationUpdatesRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomePageScreenViewModel(
            repo = repo,
            userInfo = userInfo,
            alarmScheduler = alarmScheduler,
            geofenceManager = geofenceManager,
            serviceKiller = serviceKiller,
            stringResolver = stringResolver,
            locationUpdatesRepository = locationUpdatesRepository,
        ) as T
    }
}
