package com.tasa.ui.screens.rule

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Action
import com.tasa.domain.CalendarEvent
import com.tasa.domain.RuleEvent
import com.tasa.domain.toTriggerTime
import com.tasa.repository.TasaRepo
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.StringResourceResolver
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

sealed class EditRuleState {
    data object Loading : EditRuleState()

    data class Editing(
        val newStartTime: LocalDateTime,
        val newEndTime: LocalDateTime,
    ) : EditRuleState()

    data class Success(val rule: RuleEvent) : EditRuleState()

    data class Error(val error: String) : EditRuleState()

    data object Uninitialized : EditRuleState()
}

class EditRuleViewModel(
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
    private val rule: RuleEvent,
    private val queryCalendarService: QueryCalendarService,
    private val stringResourceResolver: StringResourceResolver,
    initialState: EditRuleState = EditRuleState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<EditRuleState>(initialState)
    val state: StateFlow<EditRuleState> = _state.asStateFlow()

    var event: CalendarEvent? = null

    fun setEventTime(
        eventId: Long,
        calendarId: Long,
    ): Job? {
        if (_state.value !is EditRuleState.Uninitialized) return null
        _state.value = EditRuleState.Loading
        return viewModelScope.launch {
            try {
                val result = queryCalendarService.getEvent(eventId, calendarId)
                if (result == null) {
                    _state.value =
                        EditRuleState.Error(
                            stringResourceResolver.getString(R.string.error_on_editing_rule),
                        )
                    return@launch
                }
                event = result
                _state.value = EditRuleState.Editing(rule.startTime, rule.endTime)
            } catch (ex: Throwable) {
                Log.d("EditRuleViewModel", "Error getting event", ex)
                _state.value =
                    EditRuleState.Error(
                        stringResourceResolver.getString(R.string.error_on_editing_rule),
                    )
            }
        }
    }

    fun updateRule(
        rule: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
    ) {
        if (_state.value !is EditRuleState.Editing) return
        _state.value = EditRuleState.Loading
        viewModelScope.launch {
            try {
                val isCollision = repo.ruleRepo.isCollisionWithAnother(rule, newStartTime, newEndTime)
                if (!isCollision) {
                    if (!newStartTime.isAfter(newEndTime) &&
                        !toInterval(newStartTime, newEndTime)
                            .isWithin(toInterval(rule.startTime, rule.endTime))
                    ) {
                        _state.value =
                            EditRuleState.Error(
                                stringResourceResolver.getString(R.string.new_time_is_not_on_event_time),
                            )
                        return@launch
                    }
                    repo.ruleRepo.updateRuleEvent(
                        rule.id,
                        newStartTime,
                        newEndTime,
                        rule.startTime,
                        rule.endTime,
                    )
                    val alarmStart =
                        repo.alarmRepo.getAlarmByTriggerTime(
                            rule.startTime.toTriggerTime().value,
                        )
                    val alarmEnd =
                        repo.alarmRepo.getAlarmByTriggerTime(
                            rule.endTime.toTriggerTime().value,
                        )
                    if (alarmStart == null || alarmEnd == null) {
                        val startAlarmId =
                            repo.alarmRepo.createAlarm(
                                newStartTime.toTriggerTime().value,
                                Action.MUTE,
                            )
                        alarmScheduler.scheduleAlarm(
                            startAlarmId,
                            newStartTime.toTriggerTime(),
                            Action.MUTE,
                        )
                        val endAlarmId =
                            repo.alarmRepo.createAlarm(
                                newEndTime.toTriggerTime().value,
                                Action.UNMUTE,
                            )
                        alarmScheduler.scheduleAlarm(
                            endAlarmId,
                            newEndTime.toTriggerTime(),
                            Action.UNMUTE,
                        )
                    } else {
                        alarmScheduler.updateAlarm(
                            alarmStart.id,
                            newStartTime.toTriggerTime(),
                            alarmStart.action,
                        )
                        repo.alarmRepo.updateAlarm(
                            triggerTime = newStartTime.toTriggerTime().value,
                            action = alarmStart.action,
                            id = alarmStart.id,
                        )
                        alarmScheduler.updateAlarm(
                            alarmEnd.id,
                            newEndTime.toTriggerTime(),
                            alarmEnd.action,
                        )
                        repo.alarmRepo.updateAlarm(
                            triggerTime = newEndTime.toTriggerTime().value,
                            action = alarmEnd.action,
                            id = alarmEnd.id,
                        )
                    }
                    _state.value =
                        EditRuleState.Success(
                            rule.copy(
                                startTime = newStartTime,
                                endTime = newEndTime,
                            ),
                        )
                } else {
                    _state.value =
                        EditRuleState.Error(
                            stringResourceResolver.getString(R.string.rule_already_exists_for_this_time),
                        )
                }
            } catch (ex: Throwable) {
                _state.value =
                    EditRuleState.Error(
                        stringResourceResolver.getString(R.string.error_on_editing_rule),
                    )
                return@launch
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class EditRuleViewModelFactory(
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
    private val rule: RuleEvent,
    private val queryCalendarService: QueryCalendarService,
    private val stringResourceResolver: StringResourceResolver,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditRuleViewModel(
            repo = repo,
            alarmScheduler = alarmScheduler,
            rule = rule,
            queryCalendarService = queryCalendarService,
            stringResourceResolver = stringResourceResolver,
        ) as T
    }
}
