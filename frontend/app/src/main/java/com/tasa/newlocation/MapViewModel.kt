package com.tasa.newlocation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.Location
import com.tasa.service.LocationService
import com.tasa.utils.Failure
import com.tasa.utils.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MapScreenState {
    object Loading : MapScreenState

    object Idle : MapScreenState

    data class Success(val location: Location) : MapScreenState

    data class Error(val message: String) : MapScreenState
}

class MapScreenViewModel(
    private val locationService: LocationService,
) : ViewModel() {
    private val _state =
        MutableStateFlow<MapScreenState>(MapScreenState.Idle)
    val state = _state.asStateFlow()

    fun addLocation(location: Location) {
        if (_state.value == MapScreenState.Loading) {
            _state.value = MapScreenState.Loading
            viewModelScope.launch {
                delay(1000)
                val loc = locationService.insertLocation(location)
                when (loc) {
                    is Success -> {
                        _state.value = MapScreenState.Success(loc.value)
                    }
                    is Failure -> {
                        _state.value = MapScreenState.Error(loc.value.message)
                    }
                }
            }
        }
    }

    fun setIdleState() {
        _state.value = MapScreenState.Idle
    }
}

@Suppress("UNCHECKED_CAST")
class MapScreenViewModelFactory(
    private val locationService: LocationService,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MapScreenViewModel(
            locationService,
        ) as T
    }
}
