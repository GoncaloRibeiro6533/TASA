package com.tasa.ui.screens.calendar

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
import com.tasa.utils.Failure
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.StringResourceResolver
import com.tasa.utils.Success
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

    data class Error(val message: String) : CalendarScreenState

    data class CreatingRuleEvent(val event: CalendarEvent) : CalendarScreenState
}

class CalendarScreenViewModel(
    private val ruleScheduler: AlarmScheduler,
    private val repo: TasaRepo,
    private val queryCalendarService: QueryCalendarService,
    private val stringResolver: StringResourceResolver,
    initialState: CalendarScreenState = CalendarScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<CalendarScreenState>(initialState)
    val state = _state.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events = _events.asStateFlow()

    fun loadEvents(): Job? {
        if (_state.value == CalendarScreenState.Loading) return null
        _state.value = CalendarScreenState.Loading
        return viewModelScope.launch {
            try {
                queryCalendarService.calendarEventsFlow().collect { it ->
                    _events.value = it
                    if (_state.value is CalendarScreenState.Loading) {
                        _state.value = CalendarScreenState.Success(events)
                    }
                }
            } catch (e: Throwable) {
                _state.value =
                    CalendarScreenState.Error(
                        stringResolver.getString(R.string.unexpected_error),
                    )
                Log.e("CalendarViewModel", "Error loading events: ${e.message}")
            }
        }
    }

    fun onCreateRuleEvent(
        event: CalendarEvent,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
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
                    when (rule) {
                        is Failure -> {
                            _state.value =
                                CalendarScreenState.Error(
                                    stringResolver.getString(R.string.error_creating_rule),
                                )
                            return@launch
                        }
                        is Success -> {
                            val alarmIdStart =
                                repo.alarmRepo.createAlarm(
                                    rule.value.startTime.toTriggerTime().value,
                                    Action.MUTE,
                                )
                            ruleScheduler.scheduleAlarm(
                                alarmIdStart,
                                rule.value.startTime.toTriggerTime(),
                                Action.MUTE,
                            )
                            val alarmIdEnd =
                                repo.alarmRepo.createAlarm(
                                    rule.value.endTime.toTriggerTime().value,
                                    Action.UNMUTE,
                                )
                            ruleScheduler.scheduleAlarm(
                                alarmIdEnd,
                                rule.value.endTime.toTriggerTime(),
                                Action.UNMUTE,
                            )
                            _state.value = CalendarScreenState.SuccessOnSchedule(events)
                            Log.d("CalendarViewModel", "Rule created: ${rule.value}")
                        }
                    }
                } else {
                    _state.value =
                        CalendarScreenState.Error(
                            stringResolver.getString(R.string.rule_already_exists_for_this_time),
                        )
                }
            } catch (ex: Throwable) {
                _state.value =
                    CalendarScreenState.Error(
                        stringResolver.getString(R.string.error_creating_rule),
                    )
            }
        }
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
    private val queryCalendarService: QueryCalendarService,
    private val stringResolver: StringResourceResolver,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarScreenViewModel(
            ruleScheduler = ruleScheduler,
            repo = repo,
            queryCalendarService = queryCalendarService,
            stringResolver = stringResolver,
        ) as T
    }
}
