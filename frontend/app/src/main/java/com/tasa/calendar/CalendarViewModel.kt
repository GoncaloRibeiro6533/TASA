package com.tasa.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.service.EventService
import com.tasa.utils.Failure
import com.tasa.utils.Success
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CalendarScreenState {
    data object Idle : CalendarScreenState

    data object Loading : CalendarScreenState

    data class Success(val event: Event) : CalendarScreenState

    data class Error(val error: ApiError) : CalendarScreenState
}

class CalendarScreenViewModel(
    private val eventServices: EventService,
    initialState: CalendarScreenState = CalendarScreenState.Idle,
) : ViewModel() {
    private val _state = MutableStateFlow<CalendarScreenState>(initialState)
    val state = _state.asStateFlow()

    fun addEvent(event: Event) {
        if (_state.value != CalendarScreenState.Loading) {
            _state.value = CalendarScreenState.Loading
            viewModelScope.launch {
                _state.value =
                    try {
                        val event = eventServices.insertEvent(event)
                        when (event) {
                            is Success -> CalendarScreenState.Success(event.value)
                            is Failure -> CalendarScreenState.Error(event.value)
                        }
                    } catch (e: Throwable) {
                        CalendarScreenState.Error(ApiError("Error registering Event"))
                    }
            }
        }
    }

    fun setIdleState() {
        _state.value = CalendarScreenState.Idle
    }
}

@Suppress("UNCHECKED_CAST")
class CalendarScreenViewModelFactory(private val eventService: EventService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarScreenViewModel(eventService) as T
    }
}
