package com.tasa.newevent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.repository.TasaRepo
import com.tasa.service.EventService
import com.tasa.service.TasaService
import com.tasa.utils.Success
import com.tasa.utils.Failure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface NewEventScreenState {
    data object Idle : NewEventScreenState
    data object Loading : NewEventScreenState
    data class Success(val event: Event) : NewEventScreenState
    data class Error(val error: ApiError) : NewEventScreenState
}

class NewEventViewModel(
    private val eventService: EventService,
    initialState: NewEventScreenState = NewEventScreenState.Idle
): ViewModel() {

    private val _state = MutableStateFlow<NewEventScreenState>(initialState)
    val state = _state.asStateFlow()

    fun addEvent(event: Event){
        if(_state.value != NewEventScreenState.Loading) {
            _state.value = NewEventScreenState.Loading
            viewModelScope.launch {
                _state.value =
                    try {
                        val addEvent = eventService.insertEvent(event)
                        when (addEvent) {
                            is Success -> {
                                NewEventScreenState.Success(addEvent.value)
                            }
                            is Failure -> NewEventScreenState.Error(addEvent.value)

                        }
                    } catch (e: Throwable) {
                        NewEventScreenState.Error(ApiError("Error adding Event"))
                    }
            }
        }
    }
    fun setIdleState() {
        _state.value = NewEventScreenState.Idle
    }
}

@Suppress("UNCHECKED_CAST")
class NewEventScreenViewModelFactory(
    private val eventService: EventService
): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>):  T {
        return NewEventViewModel(
            eventService
        ) as T
    }
}
