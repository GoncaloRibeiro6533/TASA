package com.tasa.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.ApiError
import com.tasa.domain.UserInfoRepository
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationService
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import com.tasa.utils.Failure
import com.tasa.utils.ServiceKiller
import com.tasa.utils.Success
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class MenuScreenState {
    data object Idle : MenuScreenState()

    data object LoggingOut : MenuScreenState()

    data object LoggedOut : MenuScreenState()

    data class Error(val error: ApiError) : MenuScreenState()
}

class MenuViewModel(
    private val userInfo: UserInfoRepository,
    private val repo: TasaRepo,
    private val serviceKiller: ServiceKiller,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    initialState: MenuScreenState = MenuScreenState.Idle,
) : ViewModel() {
    private val _state = MutableStateFlow<MenuScreenState>(initialState)
    val state: StateFlow<MenuScreenState> = _state.asStateFlow()

    fun logout(): Job? {
        if (_state.value is MenuScreenState.LoggingOut) return null
        _state.value = MenuScreenState.LoggingOut
        return viewModelScope.launch {
            _state.value =
                try {
                    when (repo.userRepo.logout()) {
                        is Success -> {
                            clear()
                            delay(500)
                            MenuScreenState.LoggedOut
                        }
                        is Failure -> {
                            clear()
                            MenuScreenState.LoggedOut
                        }
                    }
                } catch (e: Throwable) {
                    clear()
                    MenuScreenState.LoggedOut
                }
        }
    }

    fun clear() {
        viewModelScope.launch {
            if (LocationService.isRunning) {
                serviceKiller.killServices(LocationService::class)
            }
            locationUpdatesRepository.forceStop()
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
            userInfo.clearUserInfo()
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MenuViewModelFactory(
    private val userInfo: UserInfoRepository,
    private val repo: TasaRepo,
    private val serviceKiller: ServiceKiller,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MenuViewModel(
            userInfo,
            repo,
            serviceKiller,
            locationUpdatesRepository,
            alarmScheduler,
            geofenceManager,
        ) as T
    }
}
