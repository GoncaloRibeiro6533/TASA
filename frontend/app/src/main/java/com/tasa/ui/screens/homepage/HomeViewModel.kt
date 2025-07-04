package com.tasa.ui.screens.homepage

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toTriggerTime
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationService
import com.tasa.repository.TasaRepo
import com.tasa.utils.Failure
import com.tasa.utils.ServiceKiller
import com.tasa.utils.Success
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeScreenState {
    data object Uninitialized : HomeScreenState

    data object Loading : HomeScreenState

    data class Success(val rules: StateFlow<List<Rule>>) : HomeScreenState

    data class Error(val error: Int?, val message: String = "") : HomeScreenState
}

class HomePageScreenViewModel(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    initialState: HomeScreenState = HomeScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeScreenState>(initialState)
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val _rules = MutableStateFlow<List<Rule>>(emptyList())
    val rules: StateFlow<List<Rule>> = _rules.asStateFlow()

    fun onFatalError() {
        viewModelScope.launch {
            try {
                repo.userRepo.clear()
                repo.ruleRepo.clean()
                repo.alarmRepo.clear()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                userInfo.clearUserInfo()
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
                        _state.value = HomeScreenState.Error(null, result.value.message)
                    }
                }
            } catch (e: Throwable) {
                _state.value =
                    HomeScreenState.Error(R.string.unexpected_error)
                Log.d("HomePageScreenViewModel", "loadLocalData: ${e.message}")
            }
        }
    }

    fun cancelRule(rule: Rule) {
        viewModelScope.launch {
            try {
                when (rule) {
                    is RuleEvent -> {
                        repo.ruleRepo.deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndtime(
                            rule.event.eventId,
                            rule.event.calendarId,
                            rule.startTime,
                            rule.endTime,
                        )
                        val alarmStart = repo.alarmRepo.getAlarmByTriggerTime(rule.startTime.toTriggerTime().value)
                        val alarmEnd = repo.alarmRepo.getAlarmByTriggerTime(rule.endTime.toTriggerTime().value)
                        if (alarmStart != null) {
                            repo.alarmRepo.deleteAlarm(alarmStart.id)
                            alarmScheduler.cancelAlarm(alarmStart.id)
                        }
                        if (alarmEnd != null) {
                            repo.alarmRepo.deleteAlarm(alarmEnd.id)
                            alarmScheduler.cancelAlarm(alarmEnd.id)
                        }
                    }
                    is RuleLocation -> {}
                    is RuleLocationTimeless -> {
                        val geofences =
                            repo.geofenceRepo.getAllGeofences()
                                .filter { it.name == rule.location.name }
                        if (geofences.isNotEmpty()) {
                            geofences.forEach { geofence ->
                                geofenceManager.deregisterGeofence(geofence.name)
                            }
                        }
                        repo.ruleRepo.deleteRuleLocationTimelessByLocation(rule.location)
                        repo.ruleRepo.deleteRuleLocationTimelessByLocation(rule.location)
                        repo.geofenceRepo.deleteGeofence(geofences.first())
                        if (LocationService.isRunning && LocationService.locationName ==
                            rule.location.name
                        ) {
                            serviceKiller.killServices(LocationService::class)
                        }
                    }
                }
                _state.value = HomeScreenState.Success(rules)
            } catch (e: Throwable) {
                _state.value = HomeScreenState.Error(R.string.error_on_cancel_rule)
            }
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
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomePageScreenViewModel(
            repo = repo,
            userInfo = userInfo,
            alarmScheduler = alarmScheduler,
            geofenceManager = geofenceManager,
            serviceKiller = serviceKiller,
        ) as T
    }
}
