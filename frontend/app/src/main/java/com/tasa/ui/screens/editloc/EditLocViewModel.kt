package com.tasa.ui.screens.editloc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Location
import com.tasa.repository.TasaRepo
import com.tasa.ui.screens.authentication.register.RegisterScreenState
import com.tasa.ui.screens.calendar.CalendarScreenViewModel
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.StringResourceResolver
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EditLocScreenState {
    data object Loading: EditLocScreenState

    data object Idle: EditLocScreenState

    data class Success(val newLoc: Location): EditLocScreenState

    data class Error(val error: String): EditLocScreenState
}

class EditLocScreenViewModel(
    private val repo: TasaRepo,
    initialState: EditLocScreenState = EditLocScreenState.Idle

): ViewModel() {

    private val _state = MutableStateFlow<EditLocScreenState>(initialState)
    val state: StateFlow<EditLocScreenState> = _state.asStateFlow()

    fun updateLocCenter(
        location: Location
    ) {
        if (_state.value is EditLocScreenState.Loading) return
        _state.value = EditLocScreenState.Loading
        viewModelScope.launch {
            try {
                repo.locationRepo.updateLocation(location)
            } catch (e: Exception) {
                _state.value = EditLocScreenState.Error(e.message ?: "Unknown Error")
                Log.e("EditLocViewModel", "Error updating location: ${e.message}")
            }
        }

    }

    fun editLocFields(
        name: String,
        radius: Double,
        location: Location
    ) {
        if (_state.value is EditLocScreenState.Loading) return
        _state.value = EditLocScreenState.Loading
        viewModelScope.launch {
            try {
                repo.locationRepo.updateLocationFields(name, radius,location)
            } catch (e: Exception) {
                _state.value = EditLocScreenState.Error(e.message ?: "Unknown Error")
                Log.e("EditLocViewModel", "Error updating location: ${e.message}")
            }
        }
    }

    fun setIdleState() {
        _state.value = EditLocScreenState.Idle
    }
}

@Suppress("UNCHECKED_CAST")
class EditLocScreenViewModelFactory(
    private val repo: TasaRepo,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditLocScreenViewModel(
            repo
        ) as T
    }
}