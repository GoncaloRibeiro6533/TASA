package com.tasa.ui.screens.mylocations

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.domain.Location
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationService
import com.tasa.repository.TasaRepo
import com.tasa.utils.ServiceKiller
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed interface MyLocationsScreenState {
    data object Uninitialized : MyLocationsScreenState

    data object Loading : MyLocationsScreenState

    data class Error(val resourceID: Int) : MyLocationsScreenState

    data class Success(
        val locations: StateFlow<List<Location>>,
        val messageOfSuccess: Int? = null,
    ) : MyLocationsScreenState

    data class CreatingRuleLocation(
        val location: Location,
        val startTime: LocalDateTime? = null,
        val endTime: LocalDateTime? = null,
    ) : MyLocationsScreenState
}

class MyLocationsScreenViewModel(
    private val repo: TasaRepo,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
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
                repo.locationRepo.fetchLocations().collect {
                        stream ->
                    _locations.value = stream
                    _state.value = MyLocationsScreenState.Success(_locations)
                }
            } catch (e: Throwable) {
                _state.value = MyLocationsScreenState.Error(R.string.unexpected_error)
            }
        }
    }

    fun setCreatingRuleLocationState(location: Location) {
        _state.value = MyLocationsScreenState.CreatingRuleLocation(location)
    }

    fun setSuccessState() {
        _state.value = MyLocationsScreenState.Success(_locations)
    }

    fun deleteLocation(location: Location) {
        if (_state.value is MyLocationsScreenState.Loading) return
        _state.value = MyLocationsScreenState.Loading
        viewModelScope.launch {
            try {
                val geofences = repo.geofenceRepo.getAllGeofences().filter { it.name == location.name }
                if (geofences.isNotEmpty()) {
                    geofences.forEach { geofence ->
                        geofenceManager.deregisterGeofence(geofence.name)
                    }
                    repo.ruleRepo.deleteRuleLocationTimelessByLocation(location)
                    repo.geofenceRepo.deleteGeofence(geofences.first())
                    if (LocationService.isRunning && LocationService.locationName == location.name) {
                        serviceKiller.killServices(LocationService::class)
                    }
                }
                repo.locationRepo.deleteLocationByName(location.name)
                _state.value = MyLocationsScreenState.Success(_locations)
                _successMessage.value = R.string.location_deleted
            } catch (e: Throwable) {
                Log.d("MyLocationsScreenViewModel", "deleteLocation: ", e)
                _state.value = MyLocationsScreenState.Error(R.string.unexpected_error)
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun createTimelessRuleLocation(location: Location) {
        if (_state.value is MyLocationsScreenState.Loading) return
        _state.value = MyLocationsScreenState.Loading
        viewModelScope.launch {
            try {
                val timelessRulesForLocation =
                    repo.ruleRepo.getTimelessRulesForLocation(location)
                if (timelessRulesForLocation.isNotEmpty()) {
                    _state.value =
                        MyLocationsScreenState.Error(R.string.rule_already_exists_for_this_location)
                    return@launch
                }
                // ensure radius is at least 100 meters
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
                val id =
                    repo.geofenceRepo.createGeofence(
                        location,
                    )
                repo.ruleRepo.insertRuleLocationTimeless(
                    location,
                    id.toInt(),
                )
                _state.value = MyLocationsScreenState.Success(_locations)
                _successMessage.value = R.string.rule_created_successfully
            } catch (e: Throwable) {
                _state.value = MyLocationsScreenState.Error(R.string.unexpected_error)
            }
        }
    }

    fun clearMessageOfSuccess() {
        _successMessage.value = null
    }
}

@Suppress("UNCHECKED_CAST")
class MyLocationsScreenViewModelFactory(
    private val repo: TasaRepo,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyLocationsScreenViewModel(
            repo = repo,
            geofenceManager = geofenceManager,
            serviceKiller = serviceKiller,
        ) as T
    }
}
