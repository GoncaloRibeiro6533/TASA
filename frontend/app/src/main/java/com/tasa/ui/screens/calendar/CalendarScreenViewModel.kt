package com.tasa.ui.screens.calendar

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Action
import com.tasa.domain.CalendarEvent
import com.tasa.domain.toTriggerTime
import com.tasa.repository.TasaRepo
import com.tasa.ui.screens.calendar.utils.calendarEventsFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed interface CalendarScreenState {
    data object Uninitialized : CalendarScreenState

    data object Loading : CalendarScreenState

    data class SuccessOnSchedule(
        val events: StateFlow<List<CalendarEvent>>,
    ) : CalendarScreenState

    data class Success(val events: StateFlow<List<CalendarEvent>>) : CalendarScreenState

    data class Error(val message: Int) : CalendarScreenState

    data class CreatingRuleEvent(val event: CalendarEvent) : CalendarScreenState
}

class CalendarScreenViewModel(
    private val ruleScheduler: AlarmScheduler,
    private val repo: TasaRepo,
    initialState: CalendarScreenState = CalendarScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<CalendarScreenState>(initialState)
    val state = _state.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events = _events.asStateFlow()

    fun loadEvents(activityContext: Context): Job? {
        if (_state.value == CalendarScreenState.Loading) return null
        _state.value = CalendarScreenState.Loading
        return viewModelScope.launch {
            try {
                activityContext.calendarEventsFlow().collect { it ->
                    _events.value = it
                    if (_state.value is CalendarScreenState.Loading) {
                        _state.value = CalendarScreenState.Success(events)
                    }
                }
            } catch (e: Throwable) {
                _state.value = CalendarScreenState.Error(R.string.unexpected_error)
                Log.e("CalendarViewModel", "Error loading events: ${e.message}")
            }
        }
    }

    fun onCreateRuleEvent(
        event: CalendarEvent,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
        activityContext: Context,
    ) {
        if (_state.value == CalendarScreenState.Loading) return
        _state.value = CalendarScreenState.Loading
        viewModelScope.launch {
            try {
                val collides = repo.ruleRepo.isCollision(event.startTime, event.endTime)
                if (!collides) {
                    val rule =
                        repo.ruleRepo.insertRuleEvent(
                            startTime = startTime ?: event.startTime,
                            endTime = endTime ?: event.endTime,
                            event = event.event,
                        )
                    val alarmIdStart =
                        repo.alarmRepo.createAlarm(
                            rule.startTime.toTriggerTime().value,
                            Action.MUTE,
                        )
                    ruleScheduler.scheduleAlarm(
                        alarmIdStart,
                        rule.startTime.toTriggerTime(),
                        Action.MUTE,
                    )
                    val alarmIdEnd =
                        repo.alarmRepo.createAlarm(
                            rule.endTime.toTriggerTime().value,
                            Action.UNMUTE,
                        )
                    ruleScheduler.scheduleAlarm(
                        alarmIdEnd,
                        rule.endTime.toTriggerTime(),
                        Action.UNMUTE,
                    )
                    _state.value = CalendarScreenState.SuccessOnSchedule(events)
                } else {
                    _state.value =
                        CalendarScreenState.Error(R.string.rule_already_exists_for_this_time)
                }
            } catch (ex: Throwable) {
                _state.value = CalendarScreenState.Error(R.string.error_creating_rule)
            }
        }
    }

    fun onSuccessScheduleDismiss() {
        _state.value = CalendarScreenState.Success(events)
    }

    fun onEventSelected(event: CalendarEvent) {
        _state.value = CalendarScreenState.CreatingRuleEvent(event)
    }

    fun onCancel() {
        _state.value = CalendarScreenState.Success(events)
    }
}

@Suppress("UNCHECKED_CAST")
class CalendarViewModelFactory(
    private val ruleScheduler: AlarmScheduler,
    private val repo: TasaRepo,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarScreenViewModel(
            ruleScheduler = ruleScheduler,
            repo = repo,
        ) as T
    }
}
