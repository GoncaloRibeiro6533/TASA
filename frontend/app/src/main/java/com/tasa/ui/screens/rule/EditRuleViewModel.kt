package com.tasa.ui.screens.rule

import android.content.Context
import android.provider.CalendarContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Action
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.domain.toTriggerTime
import com.tasa.repository.TasaRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

sealed class EditRuleState {
    object Loading : EditRuleState()

    data class Editing(
        val newStartTime: LocalDateTime,
        val newEndTime: LocalDateTime,
    ) : EditRuleState()

    data class Success(val rule: Rule) : EditRuleState()

    data class Error(val error: Int) : EditRuleState()
}

class EditRuleViewModel(
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
    private val rule: Rule,
    initialState: EditRuleState = EditRuleState.Editing(rule.startTime, rule.endTime),
) : ViewModel() {
    private val _state = MutableStateFlow<EditRuleState>(initialState)
    val state: StateFlow<EditRuleState> = _state.asStateFlow()

    fun updateRule(
        rule: Rule = this.rule,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        activityContext: Context,
    ) {
        if (_state.value !is EditRuleState.Editing) return
        _state.value = EditRuleState.Loading
        viewModelScope.launch {
            val isCollision = repo.ruleRepo.isCollisionWithAnother(rule, newStartTime, newEndTime)
            if (!isCollision) {
                when (rule) {
                    is RuleLocation -> {
                        // TODO
                    }
                    is RuleEvent -> {
                        if (!newStartTime.isAfter(newEndTime) &&
                            !toInterval(newStartTime, newEndTime)
                                .isWithin(toInterval(rule.startTime, rule.endTime))
                        ) {
                            _state.value = EditRuleState.Error(R.string.new_time_is_not_on_event_time)
                            return@launch
                        }
                        if (activityContext.timeIsInEventTime(
                                rule.event.id,
                                rule.event.calendarId,
                                newStartTime,
                                newEndTime,
                            )
                        ) {
                            TODO()
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
                }
            } else {
                _state.value = EditRuleState.Error(R.string.rule_already_exists_for_this_time)
            }
        }
    }

    fun setSuccess() {
        if (_state.value !is EditRuleState.Success) return
        _state.value = EditRuleState.Success(rule)
    }

    private fun Context.timeIsInEventTime(
        eventId: Long,
        calendarId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean {
        val projection =
            arrayOf(
                CalendarContract.Events._ID,
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND,
            )

        val zoneId = ZoneId.systemDefault()
        val startMillis = startTime.atZone(zoneId).toInstant().toEpochMilli()
        val endMillis = endTime.atZone(zoneId).toInstant().toEpochMilli()

        val selection =
            """
            ${CalendarContract.Events.CALENDAR_ID} = ? AND
            ${CalendarContract.Events.DTSTART} <= ? AND
            ${CalendarContract.Events.DTEND} >= ?
            """.trimIndent()

        val selectionArgs =
            arrayOf(
                calendarId.toString(),
                endMillis.toString(),
                startMillis.toString(),
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
            val dtStartIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
            val dtEndIndex = it.getColumnIndexOrThrow(CalendarContract.Events.DTEND)

            while (it.moveToNext()) {
                val eventStart = Instant.ofEpochMilli(it.getLong(dtStartIndex)).atZone(zoneId).toLocalDateTime()
                val eventEnd = Instant.ofEpochMilli(it.getLong(dtEndIndex)).atZone(zoneId).toLocalDateTime()

                if (eventStart <= startTime && eventEnd >= endTime) {
                    return true
                }
            }
        }
        return false
    }
}

@Suppress("UNCHECKED_CAST")
class EditRuleViewModelFactory(
    private val repo: TasaRepo,
    private val alarmScheduler: AlarmScheduler,
    private val rule: Rule,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditRuleViewModel(
            repo = repo,
            alarmScheduler = alarmScheduler,
            rule = rule,
        ) as T
    }
}
