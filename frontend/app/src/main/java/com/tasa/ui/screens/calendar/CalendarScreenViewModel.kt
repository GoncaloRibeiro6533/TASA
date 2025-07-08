package com.tasa.ui.screens.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tasa.R
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Action
import com.tasa.domain.CalendarEvent
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toTriggerTime
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationService
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import com.tasa.utils.Failure
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.ServiceKiller
import com.tasa.utils.StringResourceResolver
import com.tasa.utils.Success
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

sealed interface CalendarScreenState {
    data object Uninitialized : CalendarScreenState

    data object Loading : CalendarScreenState

    data class SuccessOnSchedule(
        val events: StateFlow<List<CalendarEvent>>,
        val selectedDay: StateFlow<LocalDate>,
    ) : CalendarScreenState

    data class Success(val events: StateFlow<List<CalendarEvent>>, val selectedDay: StateFlow<LocalDate>) : CalendarScreenState

    data class Error(val message: String) : CalendarScreenState

    data class CreatingRuleEvent(val event: CalendarEvent) : CalendarScreenState

    data object SessionExpired : CalendarScreenState
}

class CalendarScreenViewModel(
    private val ruleScheduler: AlarmScheduler,
    private val repo: TasaRepo,
    private val queryCalendarService: QueryCalendarService,
    private val stringResolver: StringResourceResolver,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    initialState: CalendarScreenState = CalendarScreenState.Uninitialized,
) : ViewModel() {
    private val _state = MutableStateFlow<CalendarScreenState>(initialState)
    val state = _state.asStateFlow()

    private val _events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val events = _events.asStateFlow()

    private val _selectedDay = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedDay: StateFlow<LocalDate> = _selectedDay.asStateFlow()

    private val _successMessage = MutableStateFlow<Int?>(null)
    val successMessage: StateFlow<Int?> = _successMessage.asStateFlow()

    fun loadEvents(): Job? {
        if (_state.value == CalendarScreenState.Loading) return null
        _state.value = CalendarScreenState.Loading
        return viewModelScope.launch {
            try {
                queryCalendarService.calendarEventsFlow().collect { it ->
                    _events.value = it
                    if (_state.value is CalendarScreenState.Loading) {
                        _state.value = CalendarScreenState.Success(events, selectedDay)
                    }
                }
            } catch (e: Throwable) {
                if (e !is CancellationException) {
                    _state.value =
                        CalendarScreenState.Error(
                            stringResolver.getString(R.string.unexpected_error),
                        )
                }
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
                if (event.title.isBlank()) {
                    _state.value =
                        CalendarScreenState.Error(
                            stringResolver.getString(R.string.event_title_required),
                        )
                    return@launch
                }
                val collides = repo.ruleRepo.isCollision(event.startTime, event.endTime)
                if (!collides) {
                    val eventLocal =
                        repo.eventRepo.getByCalendarIdAndEventId(
                            event.calendarId,
                            event.eventId,
                        )
                    val eventResult =
                        if (eventLocal == null) {
                            val result =
                                repo.eventRepo.insertEvent(
                                    calendarId = event.calendarId,
                                    eventId = event.eventId,
                                    title = event.title,
                                    startTime = startTime ?: event.startTime,
                                    endTime = endTime ?: event.endTime,
                                )
                            when (result) {
                                is Failure -> {
                                    _state.value =
                                        CalendarScreenState.Error(
                                            result.value.message,
                                        )
                                    return@launch
                                }
                                is Success -> {
                                    result.value
                                }
                            }
                        } else {
                            eventLocal
                        }
                    val rule =
                        repo.ruleRepo.insertRuleEvent(
                            startTime = startTime ?: event.startTime,
                            endTime = endTime ?: event.endTime,
                            event = eventResult,
                        )
                    when (rule) {
                        is Failure -> {
                            _state.value =
                                CalendarScreenState.Error(
                                    rule.value.message,
                                )
                            return@launch
                        }
                        is Success -> {
                            val alarmIdStart =
                                repo.alarmRepo.createAlarm(
                                    rule.value.startTime.toTriggerTime().value,
                                    Action.MUTE,
                                    rule.value.id,
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
                                    rule.value.id,
                                )
                            ruleScheduler.scheduleAlarm(
                                alarmIdEnd,
                                rule.value.endTime.toTriggerTime(),
                                Action.UNMUTE,
                            )
                            _successMessage.value = R.string.rule_created_successfully
                            _state.value = CalendarScreenState.SuccessOnSchedule(events, selectedDay)
                        }
                    }
                } else {
                    _state.value =
                        CalendarScreenState.Error(
                            stringResolver.getString(R.string.rule_already_exists_for_this_time),
                        )
                }
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                return@launch
            } catch (ex: Throwable) {
                Log.e("CalendarViewModel", "Error creating rule: ${ex.message}")
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
        _state.value = CalendarScreenState.Success(events, selectedDay)
    }

    fun onDaySelected(day: LocalDate) {
        _selectedDay.value = day
    }

    fun clearMessageOfSuccess() {
        _successMessage.value = null
    }

    fun onFatalError(): Job? {
        if (_state.value !is CalendarScreenState.SessionExpired) return null
        return clearOnFatalError()
    }

    fun clearOnFatalError() =
        viewModelScope.launch {
            try {
                userInfo.clearUserInfo()
                locationUpdatesRepository.forceStop()
                if (LocationService.isRunning) {
                    serviceKiller.killServices(LocationService::class)
                }
                val alarms = repo.alarmRepo.getAllAlarms()
                alarms.forEach { alarm ->
                    alarmScheduler.cancelAlarm(alarm.id, alarm.action)
                    repo.alarmRepo.deleteAlarm(alarm.id)
                }
                val geofences = repo.geofenceRepo.getAllGeofences()
                geofences.forEach { geofence ->
                    geofenceManager.deregisterGeofence(geofence.name)
                    repo.geofenceRepo.deleteGeofence(geofence)
                }
                repo.userRepo.clear()
                repo.ruleRepo.clean()
                repo.eventRepo.clear()
                repo.locationRepo.clear()
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            } catch (e: Throwable) {
                userInfo.clearUserInfo()
            }
        }
}

@Suppress("UNCHECKED_CAST")
class CalendarViewModelFactory(
    private val ruleScheduler: AlarmScheduler,
    private val repo: TasaRepo,
    private val queryCalendarService: QueryCalendarService,
    private val stringResolver: StringResourceResolver,
    private val locationUpdatesRepository: LocationUpdatesRepository,
    private val userInfo: UserInfoRepository,
    private val alarmScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val serviceKiller: ServiceKiller,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CalendarScreenViewModel(
            ruleScheduler = ruleScheduler,
            repo = repo,
            queryCalendarService = queryCalendarService,
            stringResolver = stringResolver,
            userInfo = userInfo,
            alarmScheduler = alarmScheduler,
            geofenceManager = geofenceManager,
            serviceKiller = serviceKiller,
            initialState = CalendarScreenState.Uninitialized,
            locationUpdatesRepository = locationUpdatesRepository,
        ) as T
    }
}
