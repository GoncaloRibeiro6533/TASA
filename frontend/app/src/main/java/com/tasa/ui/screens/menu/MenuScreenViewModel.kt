package com.tasa.ui.screens.menu

import android.util.Log
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
            try {
                when (repo.userRepo.logout()) {
                    is Success -> {
                        clear()
                        _state.value = MenuScreenState.LoggedOut
                    }
                    is Failure -> {
                        clear()
                        _state.value = MenuScreenState.LoggedOut
                    }
                }
            } catch (e: Throwable) {
                Log.e("MenuViewModel", "Error logging out", e)
                repo.ruleRepo.clean()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
                repo.userRepo.clear()
                userInfo.clearUserInfo()
                _state.value = MenuScreenState.LoggedOut
            }
        }
    }

    suspend fun clear() {
        try {
            if (LocationService.isRunning) {
                serviceKiller.killServices(LocationService::class)
            }
            val alarms = repo.alarmRepo.getAllAlarms()
            val geofences = repo.geofenceRepo.getAllGeofences()
            locationUpdatesRepository.forceStop()
            repo.ruleRepo.clean()
            repo.eventRepo.clear()
            repo.locationRepo.clear()
            repo.userRepo.clear()
            alarms.forEach { alarm ->
                alarmScheduler.cancelAlarm(alarm.id, alarm.action)
                repo.alarmRepo.deleteAlarm(alarm.id)
            }
            geofences.forEach { geofence ->
                geofenceManager.deregisterGeofence(geofence.name)
                repo.geofenceRepo.deleteGeofence(geofence)
            }
        } catch (e: Throwable) {
        } finally {
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
