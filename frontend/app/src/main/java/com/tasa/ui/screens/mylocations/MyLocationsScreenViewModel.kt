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

sealed interface MyLocationsScreenState {
    data object Uninitialized : MyLocationsScreenState

    data object Loading : MyLocationsScreenState

    data class Error(val message: String) : MyLocationsScreenState

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
    private val stringResolver: StringResourceResolver,
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
                _state.value = MyLocationsScreenState.Error(stringResolver.getString(R.string.unexpected_error))
            }
        }
    }

    fun setCreatingRuleLocationState(location: Location) {
        _state.value = MyLocationsScreenState.CreatingRuleLocation(location)
    }

    fun deleteLocation(location: Location) {
        if (_state.value is MyLocationsScreenState.Loading) return
        _state.value = MyLocationsScreenState.Loading
        viewModelScope.launch {
            try {
                val geofences = repo.geofenceRepo.getAllGeofences().filter { it.name == location.name }
                geofences.forEach { geofence ->
                    geofenceManager.deregisterGeofence(geofence.name)
                }
                val rule =
                    repo.ruleRepo.getTimelessRulesForLocation(location)
                if (rule.isNotEmpty()) {
                    rule.forEach {
                        when (val result = repo.ruleRepo.deleteRuleLocationTimeless(it)) {
                            is Failure -> {
                                _state.value = MyLocationsScreenState.Error(result.value.message)
                                return@launch
                            }
                            is Success -> {
                                repo.geofenceRepo.deleteGeofence(geofences.first())
                                if (LocationService.isRunning && LocationService.locationName == location.name) {
                                    serviceKiller.killServices(LocationService::class)
                                }
                                when (val result = repo.locationRepo.deleteLocation(location)) {
                                    is Success -> {
                                        _state.value = MyLocationsScreenState.Success(_locations)
                                        _successMessage.value = R.string.location_deleted
                                        return@launch
                                    }
                                    is Failure -> {
                                        _state.value = MyLocationsScreenState.Error(result.value.message)
                                        return@launch
                                    }
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
            } catch (e: Throwable) {
                Log.d("MyLocationsScreenViewModel", "deleteLocation: ", e)
                _state.value = MyLocationsScreenState.Error(stringResolver.getString(R.string.unexpected_error))
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
}

@Suppress("UNCHECKED_CAST")
class MyLocationsScreenViewModelFactory(
    private val repo: TasaRepo,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    private val stringResolver: StringResourceResolver,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyLocationsScreenViewModel(
            repo = repo,
            geofenceManager = geofenceManager,
            serviceKiller = serviceKiller,
            stringResolver = stringResolver,
        ) as T
    }
}
