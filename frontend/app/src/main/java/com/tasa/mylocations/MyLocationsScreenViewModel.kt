package com.tasa.mylocations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.Location
import com.tasa.repository.TasaRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MyLocationsScreenState {
    data object Uninitialized : MyLocationsScreenState

    data object Loading : MyLocationsScreenState

    data class Error(val message: String) : MyLocationsScreenState

    data class Success(val locations: StateFlow<List<Location>>) : MyLocationsScreenState
}

class MyLocationsScreenViewModel(
    private val repo: TasaRepo,
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
                _state.value = MyLocationsScreenState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class MyLocationsScreenViewModelFactory(
    private val repo: TasaRepo,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MyLocationsScreenViewModel(
            repo = repo,
            initialState = MyLocationsScreenState.Uninitialized,
        ) as T
    }
}
