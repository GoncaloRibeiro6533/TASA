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
import com.tasa.repository.TasaRepo
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

    data class Success(val locations: StateFlow<List<Location>>) : MyLocationsScreenState

    data class CreatingRuleLocation(
        val location: Location,
        val startTime: LocalDateTime? = null,
        val endTime: LocalDateTime? = null,
    ) : MyLocationsScreenState
}

class MyLocationsScreenViewModel(
    private val repo: TasaRepo,
    private val geofenceManager: GeofenceManager,
    initialState: MyLocationsScreenState = MyLocationsScreenState.Uninitialized,
) : ViewModel() {
    private val _state: MutableStateFlow<MyLocationsScreenState> = MutableStateFlow(initialState)
    val state: StateFlow<MyLocationsScreenState> = _state.asStateFlow()

    private val _locations: MutableStateFlow<List<Location>> = MutableStateFlow(emptyList())
    val locations: StateFlow<List<Location>> = _locations.asStateFlow()

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
            } catch (e: Exception) {
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

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    fun createRulesForLocation(
        location: Location,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ) {
        if (_state.value is MyLocationsScreenState.Loading) return
        _state.value = MyLocationsScreenState.Loading
        viewModelScope.launch {
            try {
                val collides = repo.ruleRepo.isCollision(startTime, endTime)
                val otherRulesForLocation = repo.ruleRepo.getRulesForLocation(location)
                if (otherRulesForLocation.isNotEmpty()) {
                    _state.value =
                        MyLocationsScreenState.Error(R.string.rule_already_exists_for_this_location)
                    return@launch
                }
                if (!collides) {
                    // TODO remove
                    geofenceManager.deregisterAllGeofences()
                    geofenceManager.registerGeofence(
                        location.name,
                        location.toLocation(),
                        location.radius.toFloat(),
                    )
                    val id =
                        repo.geofenceRepo.createGeofence(
                            location,
                        )
                    repo.ruleRepo.insertRuleLocation(
                        startTime,
                        endTime,
                        location,
                        id.toInt(),
                    )
                    _state.value = MyLocationsScreenState.Success(_locations)
                } else {
                    _state.value =
                        MyLocationsScreenState.Error(R.string.rule_already_exists_for_this_time)
                }
            } catch (e: Exception) {
                Log.d("MyLocationsScreenViewModel", "createRulesForLocation: ", e)
                _state.value = MyLocationsScreenState.Error(R.string.unexpected_error)
            }
        }
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
                    repo.ruleRepo.deleteRuleLocationByName(location.name)
                    repo.geofenceRepo.deleteGeofence(geofences.first())
                }
                repo.locationRepo.deleteLocationByName(location.name)
                _state.value = MyLocationsScreenState.Success(_locations)
            } catch (e: Exception) {
                Log.d("MyLocationsScreenViewModel", "deleteLocation: ", e)
                _state.value = MyLocationsScreenState.Error(R.string.unexpected_error)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MyLocationsScreenViewModelFactory(
    private val repo: TasaRepo,
    private val geofenceManager: GeofenceManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyLocationsScreenViewModel(
            repo = repo,
            geofenceManager = geofenceManager,
            initialState = MyLocationsScreenState.Uninitialized,
        ) as T
    }
}
