package com.tasa.ui.screens.rule

import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Action
import com.tasa.domain.CalendarEvent
import com.tasa.domain.RuleEvent
import com.tasa.domain.toLocalDateTime
import com.tasa.domain.toTriggerTime
import com.tasa.repository.TasaRepo
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

    data class Error(val error: Int) : EditRuleState()

    data object Uninitialized : EditRuleState()
}

class EditRuleViewModel(
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
    private val rule: RuleEvent,
    initialState: EditRuleState = EditRuleState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<EditRuleState>(initialState)
    val state: StateFlow<EditRuleState> = _state.asStateFlow()

    var event: CalendarEvent? = null

    fun setEventTime(
        eventId: Long,
        calendarId: Long,
        activityContext: Context,
    ): Job? {
        if (_state.value !is EditRuleState.Uninitialized) return null
        _state.value = EditRuleState.Loading
        return viewModelScope.launch {
            try {
                val result = activityContext.getEvent(eventId, calendarId)
                if (result == null) {
                    _state.value = EditRuleState.Error(R.string.error_on_editing_rule)
                    return@launch
                }
                event = result
                _state.value = EditRuleState.Editing(rule.startTime, rule.endTime)
            } catch (ex: Throwable) {
                Log.d("EditRuleViewModel", "Error getting event", ex)
                _state.value = EditRuleState.Error(R.string.error_on_editing_rule)
            }
        }
    }

    fun updateRule(
        rule: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        activityContext: Context,
    ) {
        if (_state.value !is EditRuleState.Editing) return
        _state.value = EditRuleState.Loading
        viewModelScope.launch {
            try {
                val isCollision = repo.ruleRepo.isCollisionWithAnother(rule, newStartTime, newEndTime)
                if (!isCollision) {
                    // when (rule) {
                        /*is RuleLocation -> {
                            // TODO
                        }*/
                    //  is RuleEvent -> {
                    if (!newStartTime.isAfter(newEndTime) &&
                        !toInterval(newStartTime, newEndTime)
                            .isWithin(toInterval(rule.startTime, rule.endTime))
                    ) {
                        _state.value = EditRuleState.Error(R.string.new_time_is_not_on_event_time)
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
                        alarmScheduler.scheduleAlarm(
                            newStartTime.toTriggerTime(),
                            Action.MUTE,
                            activityContext,
                        )
                        alarmScheduler.scheduleAlarm(
                            newEndTime.toTriggerTime(),
                            Action.UNMUTE,
                            activityContext,
                        )
                    } else {
                        alarmScheduler.updateAlarm(
                            alarmStart.id,
                            newStartTime.toTriggerTime(),
                            alarmStart.action,
                            activityContext,
                        )
                        alarmScheduler.updateAlarm(
                            alarmEnd.id,
                            newEndTime.toTriggerTime(),
                            alarmEnd.action,
                            activityContext,
                        )
                    }
                    _state.value =
                        EditRuleState.Success(
                            rule.copy(
                                startTime = newStartTime,
                                endTime = newEndTime,
                            ),
                        )
                }
                // }
                else {
                    _state.value = EditRuleState.Error(R.string.rule_already_exists_for_this_time)
                }
            } catch (ex: Throwable) {
                _state.value = EditRuleState.Error(R.string.error_on_editing_rule)
                return@launch
            }
        }
    }

    fun setSuccess() {
        if (_state.value !is EditRuleState.Success) return
        _state.value = EditRuleState.Success(rule)
    }

    private fun Context.getEvent(
        eventId: Long,
        calendarId: Long,
    ): CalendarEvent? {
        val projection =
            arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
                CalendarContract.Events.DESCRIPTION,
            )

        val selection =
            """
            ${CalendarContract.Events._ID} = ? AND
            ${CalendarContract.Events.CALENDAR_ID} = ?
            """.trimIndent()

        val selectionArgs =
            arrayOf(
                eventId.toString(),
                calendarId.toString(),
            )

        val cursor =
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null,
            )
        cursor?.use {
            val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
            val calendarIdIndex = it.getColumnIndex(CalendarContract.Events.CALENDAR_ID)
            val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
            val dtStartIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val dtEndIndex = it.getColumnIndex(CalendarContract.Events.DTEND)
            val descriptionIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)

            while (it.moveToNext()) {
                val description = it.getString(descriptionIndex)?.lowercase() ?: ""
                if (description.contains("feriado") || description.contains("comemoração") ||
                    description.contains("data comemorativa") || description.contains("feriados")
                ) {
                    continue
                }

                return CalendarEvent(
                    eventId = it.getLong(idIndex),
                    calendarId = it.getLong(calendarIdIndex),
                    title = it.getString(titleIndex) ?: "Sem título",
                    startTime = it.getLong(dtStartIndex).toLocalDateTime(),
                    endTime = it.getLong(dtEndIndex).toLocalDateTime(),
                )
            }
        }
        return null
    }
}

@Suppress("UNCHECKED_CAST")
class EditRuleViewModelFactory(
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
    private val rule: RuleEvent,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditRuleViewModel(
            repo = repo,
            alarmScheduler = alarmScheduler,
            rule = rule,
        ) as T
    }
}
