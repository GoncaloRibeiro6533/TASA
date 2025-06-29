package com.tasa.ui.screens.homepage

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.RuleBase
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toTriggerTime
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationService
import com.tasa.repository.TasaRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeScreenState {
    data object Uninitialized : HomeScreenState

    data object Loading : HomeScreenState

    data class Success(val rules: StateFlow<List<RuleBase>>) : HomeScreenState

    data class Error(val error: Int) : HomeScreenState
}

class HomePageScreenViewModel(
    private val repo: TasaRepo,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    initialState: HomeScreenState = HomeScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<HomeScreenState>(initialState)
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    private val _rules = MutableStateFlow<List<RuleBase>>(emptyList())
    val rules: StateFlow<List<RuleBase>> = _rules.asStateFlow()

    /*
    fun onFatalError() {
        viewModelScope.launch {
            try {
                repo.userRepo.clear()
                repo.ruleRepo.clean()
                repo.alarmRepo.clear()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                userInfo.clearUserInfo()
            } catch (e: Exception) {
                repo.userRepo.clear()
                repo.ruleRepo.clean()
                repo.alarmRepo.clear()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                userInfo.clearUserInfo()
            }
        }
    }*/

    @RequiresPermission(
        allOf = [
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ],
    )
    fun registerGeofences() {
        viewModelScope.launch {
            try {
                if (repo.geofenceRepo.getAllGeofences().isNotEmpty()) {
                    geofenceManager.onBootRegisterGeofences()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun loadLocalData(): Job? {
        if (_state.value is HomeScreenState.Loading) return null
        _state.value = HomeScreenState.Loading
        return viewModelScope.launch {
            try {
                repo.ruleRepo.fetchAllRules().collect { stream ->
                    _rules.value = stream
                    _state.value = HomeScreenState.Success(rules)
                }
            } catch (e: Throwable) {
                _state.value =
                    HomeScreenState.Error(R.string.unexpected_error)
            }
        }
    }

    fun cancelRule(
        rule: RuleBase,
        context: Context,
    ) {
        viewModelScope.launch {
            try {
                when (rule) {
                    is RuleEvent -> {
                        repo.ruleRepo.deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndtime(
                            rule.event.id,
                            rule.event.calendarId,
                            rule.startTime,
                            rule.endTime,
                        )
                        val alarmStart = repo.alarmRepo.getAlarmByTriggerTime(rule.startTime.toTriggerTime().value)
                        val alarmEnd = repo.alarmRepo.getAlarmByTriggerTime(rule.endTime.toTriggerTime().value)
                        if (alarmStart != null) {
                            repo.alarmRepo.deleteAlarm(alarmStart.id)
                            alarmScheduler.cancelAlarm(alarmStart.id, context)
                        }
                        if (alarmEnd != null) {
                            repo.alarmRepo.deleteAlarm(alarmEnd.id)
                            alarmScheduler.cancelAlarm(alarmEnd.id, context)
                        }
                    }
                    is RuleLocation -> {
                        repo.ruleRepo.deleteRuleLocationByName(rule.location.name)
                        val rules = repo.ruleRepo.getRulesForLocation(rule.location)
                        if (rules.isEmpty()) {
                            val geofences =
                                repo.geofenceRepo.getAllGeofences()
                                    .filter { it.name == rule.location.name }
                            if (geofences.isNotEmpty()) {
                                geofenceManager.deregisterGeofence(geofences.first().name)
                                repo.geofenceRepo.deleteGeofence(geofences.first())
                            }
                        }
                        if (LocationService.isRunning && LocationService.locationName ==
                            rule.location.name
                        ) {
                            val serviceIntent =
                                Intent(context, LocationService::class.java).apply {
                                    putExtra("requestId", LocationService.reqId)
                                }
                            context.stopService(serviceIntent)
                        }
                    }
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
                            val serviceIntent =
                                Intent(context, LocationService::class.java).apply {
                                    putExtra("requestId", LocationService.reqId)
                                }
                            context.stopService(serviceIntent)
                        }
                    }
                }
                _state.value = HomeScreenState.Success(rules)
            } catch (e: Exception) {
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
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomePageScreenViewModel(
            repo = repo,
            userInfo = userInfo,
            alarmScheduler = alarmScheduler,
            geofenceManager = geofenceManager,
        ) as T
    }
}
