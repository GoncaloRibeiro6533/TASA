package com.tasa.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Action
import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.TasaException
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toTriggerTime
import com.tasa.geofence.GeofenceManager
import com.tasa.repository.interfaces.RuleRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.service.http.models.event.EventInput
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.AlarmEntity
import com.tasa.storage.entities.GeofenceEntity
import com.tasa.storage.entities.RuleEventEntity
import com.tasa.storage.entities.RuleLocationTimelessEntity
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import kotlin.collections.filter
import kotlin.collections.map

class RuleRepository(
    private val local: TasaDB,
    private val remote: TasaService,
    private val userInfoRepository: UserInfoRepository,
    private val ruleScheduler: AlarmScheduler,
    private val geofenceManager: GeofenceManager,
    private val queryCalendarService: QueryCalendarService,
) : RuleRepositoryInterface {
    private suspend fun getToken(): String {
        return userInfoRepository.getToken() ?: throw AuthenticationException(
            "User is not authenticated. Please log in again.",
            null,
        )
    }

    private suspend fun getFromApi() = remote.ruleService.fetchRules(getToken())

    private suspend fun hasRules(): Boolean {
        return local.ruleEventDao().hasRules() || local.ruleLocationDao().hasRules()
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun fetchAllRules(): Either<ApiError, Flow<List<Rule>>> {
        return if (hasRules() || userInfoRepository.isLocal()) {
            success(getFromLocal())
        } else {
            when (val result = getFromApi()) {
                is Success -> {
                    val ruleEvents = result.value.eventRules
                    val events =
                        ruleEvents.mapNotNull { it ->
                            queryCalendarService.toLocalEvent(
                                it.event.id,
                                it.event.title,
                                it.event.startTime,
                                it.event.endTime,
                            )
                        }
                    local.eventDao().insertEvents(
                        *events.map { event -> event.toEventEntity() }.toTypedArray(),
                    )
                    local.ruleEventDao().insertRuleEvents(
                        ruleEvents.mapNotNull { ruleEvent ->
                            val event = events.firstOrNull { e -> e.id == ruleEvent.event.id }
                            if (event != null) {
                                RuleEventEntity(
                                    id = ruleEvent.id,
                                    startTime = ruleEvent.startTime,
                                    endTime = ruleEvent.endTime,
                                    eventId = event.eventId,
                                    calendarId = event.calendarId,
                                )
                            } else {
                                null
                            }
                        },
                    )
                    ruleEvents.forEach {
                        val alarmIdStart =
                            local.alarmDao().insertAlarm(
                                AlarmEntity(
                                    id = 0,
                                    triggerTime = it.startTime.toTriggerTime().value,
                                    action = Action.MUTE,
                                ),
                            ).toInt()
                        ruleScheduler.scheduleAlarm(
                            alarmIdStart,
                            it.startTime
                                .toTriggerTime(),
                            Action.MUTE,
                        )
                        val alarmIdEnd =
                            local.alarmDao().insertAlarm(
                                AlarmEntity(
                                    id = 0,
                                    triggerTime =
                                        it.endTime
                                            .toTriggerTime().value,
                                    action = Action.UNMUTE,
                                ),
                            ).toInt()
                        ruleScheduler.scheduleAlarm(
                            alarmIdEnd,
                            it.endTime.toTriggerTime(),
                            Action.UNMUTE,
                        )
                    }
                    val ruleLocations = result.value.locationRules
                    val locations = ruleLocations.map { it.location }
                    local.locationDao().insertLocations(
                        locations.map { it.toEntity() },
                    )
                    local.ruleLocationTimelessDao().insertRuleLocations(
                        ruleLocations.map {
                            RuleLocationTimelessEntity(
                                id = it.location.id,
                                locationName = it.location.name,
                            )
                        },
                    )
                    ruleLocations.forEach {
                        val radius =
                            if (it.location.radius < 100) {
                                100f
                            } else {
                                it.location.radius.toFloat()
                            }
                        geofenceManager.registerGeofence(
                            key = it.location.name,
                            location = it.location.toLocation(),
                            radiusInMeters = radius,
                        )
                        local.geofenceDao().insertGeofence(
                            GeofenceEntity(
                                id = 0,
                                // Auto-generated by Room
                                name = it.location.name,
                                latitude = it.location.latitude,
                                longitude = it.location.longitude,
                                radius = it.location.radius,
                            ),
                        )
                    }
                    success(getFromLocal())
                }
                is Failure -> failure(result.value)
            }
        }
    }

    private fun getFromLocal(): Flow<List<Rule>> {
        val now = LocalDateTime.now()
        val ruleEventFlow =
            local.ruleEventDao().getAllRuleEvents()
                .map { list ->
                    list.map { it.toRuleEvent() }
                        .filter { it.endTime.isAfter(now) }.sortedBy { it.startTime }
                }

        val ruleLocationFlow =
            local.ruleLocationDao().getAllRuleLocations()
                .map { list ->
                    list.map { it.toRuleLocation() }
                        .filter { it.endTime.isAfter(now) }.sortedBy { it.startTime }
                }

        val timelessRuleLocationFlow =
            local.ruleLocationTimelessDao().getAllRuleLocations()
                .map { list ->
                    list.map { it.toRuleLocationTimeless() }
                }

        return combine(
            ruleEventFlow,
            ruleLocationFlow,
            timelessRuleLocationFlow,
        ) { events, locations, timeless ->
            (events + locations + timeless)
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun fetchRuleEvents(): Flow<List<RuleEvent>> {
        return local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } }
    }

    override suspend fun fetchRuleLocations(): Flow<List<RuleLocationTimeless>> {
        return local.ruleLocationTimelessDao().getAllRuleLocations().map { it.map { it.toRuleLocationTimeless() } }
    }

    override suspend fun fetchRuleLocationsByName(name: String): List<RuleLocationTimeless> {
        return local.ruleLocationTimelessDao().getRuleLocationsByLocationNameResult(name)
            .map { it.toRuleLocationTimeless() }
    }

    override suspend fun fetchRuleByTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Rule? {
        val ruleEvent = local.ruleEventDao().getRuleEventByStartAndEndTime(startTime, endTime)
        if (ruleEvent != null) {
            return ruleEvent.toRuleEvent()
        }
        return null
    }

    override suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean {
        val ruleEvent = local.ruleEventDao().getRuleEventByStartAndEndTime(startTime, endTime)
        return ruleEvent != null
    }

    override suspend fun isCollisionWithAnother(
        rule: Rule,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
    ): Boolean {
        val result =
            local.ruleEventDao().getRuleEventByStartAndEndTime(
                newStartTime,
                newEndTime,
            ) ?: return false
        return result != rule
    }

    override suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): Either<ApiError, RuleEvent> {
        val localEvent = local.eventDao().getEventByIdSync(event.eventId, event.calendarId)
        if (localEvent == null) local.eventDao().insertEvents(event.toEventEntity())
        if (userInfoRepository.isLocal())
            {
                val ruleEvent =
                    RuleEvent(
                        startTime = startTime,
                        endTime = endTime,
                        event = event,
                    )
                local.ruleEventDao().insertRuleEvent(ruleEvent.toRuleEventEntity())
                return success(ruleEvent)
            } else {
            val event =
                if (event.id != null) {
                    event
                } else {
                    run {
                        val localEvent =
                            queryCalendarService.getEvent(event.eventId, event.calendarId)
                                ?: throw IllegalArgumentException("Event not found in local calendar")
                        val result =
                            remote.eventService.insertEvent(
                                EventInput(
                                    title = event.title,
                                    startTime = localEvent.startTime,
                                    endTime = localEvent.endTime,
                                ),
                                getToken(),
                            )
                        when (result) {
                            is Success -> {
                                local.eventDao().insertEvents(
                                    result.value.toEvent(event.eventId, event.calendarId).toEventEntity(),
                                )
                                local.eventDao().getEventByIdSync(event.eventId, event.calendarId)?.toEvent()
                                    ?: throw TasaException("Event not found after insertion", null)
                            }
                            is Failure -> return failure(result.value)
                        }
                    }
                }
            return when (
                val rule =
                    remote.ruleService.insertRuleEvent(
                        RuleEvent(
                            startTime = startTime,
                            endTime = endTime,
                            event = event,
                        ),
                        getToken(),
                    )
            ) {
                is Success -> {
                    val ruleEventEntity = rule.value.toRuleEventEntity()
                    local.ruleEventDao().insertRuleEvent(ruleEventEntity)
                    success(rule.value)
                }
                is Failure -> failure(rule.value)
            }
        }
    }

    override suspend fun deleteRuleEventById(id: Int) {
        local.ruleEventDao().deleteRuleEventById(id)
        remote.ruleService.deleteRuleEventById(id, getToken())
    }

    override suspend fun deleteRuleEventByCalendarIdAndEventId(
        eventId: Long,
        calendarId: Long,
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun cleanOldRules(now: LocalDateTime) {
        local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } }
            .collect { rules ->
                val rulesToDelete = rules.filter { it.endTime.isBefore(now) }
                val events = rulesToDelete.map { it.event }
                val eventsNotToDelete =
                    rules.filter { it !in rulesToDelete && it.event in events }.map { it.event }
                events.filter { it !in eventsNotToDelete }.forEach {
                    local.eventDao().deleteEvent(it.eventId, it.calendarId)
                }
                rulesToDelete.forEach {
                    local.ruleEventDao().deleteRuleEventByStartAndEndTime(it.startTime, it.endTime)
                }
            }
        local.ruleLocationDao().getAllRuleLocations().map { it.map { it.toRuleLocation() } }
            .collect { rules ->
                val rulesToDelete = rules.filter { it.endTime.isBefore(now) }
                rulesToDelete.forEach {
                    local.ruleLocationDao().deleteRuleLocationByName(it.location.name)
                }
            }
    }

    override suspend fun clean() {
        local.ruleEventDao().clear()
        local.ruleLocationDao().clear()
        local.ruleLocationTimelessDao().clear()
    }

    override suspend fun updateRuleEvent(
        rule: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        oldStartTime: LocalDateTime,
        oldEndTime: LocalDateTime,
    ): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal())
            {
                local.ruleEventDao().updateRuleEventByStartAndEndTime(
                    newStartTime,
                    newEndTime,
                    oldStartTime,
                    oldEndTime,
                )
                return success(Unit)
            }
        if (rule.id != null) {
            val remote =
                remote.ruleService.updateRuleEvent(
                    rule,
                    newStartTime,
                    newEndTime,
                    getToken(),
                )
            when (remote) {
                is Success -> {
                    local.ruleEventDao().updateRuleEvent(
                        rule.id,
                        newStartTime,
                        newEndTime,
                    )
                    return success(Unit)
                }
                is Failure -> return failure(remote.value)
            }
        } else {
            when (
                val result =
                    insertRuleEvent(
                        oldEndTime,
                        newEndTime,
                        rule.event,
                    )
            ) {
                is Success -> {
                    local.ruleEventDao().updateRuleEventByStartAndEndTime(
                        newStartTime,
                        newEndTime,
                        oldStartTime,
                        oldEndTime,
                    )
                    return success(Unit)
                }
                is Failure -> return failure(result.value)
            }
        }
    }

    override suspend fun deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndtime(
        eventId: Long,
        calendarId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ) {
        //TODO
        local.ruleEventDao().deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndTime(
            eventId,
            calendarId,
            startTime,
            endTime,
        )
    }

    override suspend fun getTimelessRulesForLocation(location: Location): List<RuleLocationTimeless> {
        return local.ruleLocationTimelessDao().getRuleLocationsByLocationNameResult(location.name)
            .map { it.toRuleLocationTimeless() }
    }

    override suspend fun insertRuleLocationTimeless(location: Location): Either<ApiError, RuleLocationTimeless> {
        val ruleLocationTimeless = RuleLocationTimeless(location = location)
        if (userInfoRepository.isLocal()) {
            local.ruleLocationTimelessDao()
                .insertRuleLocationTimeless(ruleLocationTimeless.toEntity())
            return success(ruleLocationTimeless)
        }
        val locationID =
            if (location.id != null) {
                location
            } else {
                run {
                    val remote = remote.locationService.insertLocation(location, getToken())
                    when (remote) {
                        is Success -> {
                            local.locationDao().insertLocation(remote.value.toEntity())
                            remote.value
                        }
                        is Failure -> {
                            return failure(remote.value)
                        }
                    }
                }
            }
        val remote =
            remote.ruleService.insertRuleLocation(
                RuleLocationTimeless(
                    location = locationID,
                ),
                getToken(),
            )
        when (remote) {
            is Success -> {
                local.ruleLocationTimelessDao()
                    .insertRuleLocationTimeless(remote.value.toEntity())
                return success(remote.value)
            }
            is Failure -> return failure(remote.value)
        }
    }

    override suspend fun getRuleLocationTimelessById(id: Int): RuleLocationTimeless? {
        return local.ruleLocationTimelessDao().getRuleLocationByIdResult(id)?.toRuleLocationTimeless()
    }

    override suspend fun deleteRuleLocationTimelessByLocation(location: Location) {
        //TODO
        local.ruleLocationTimelessDao().deleteRuleLocationByName(location.name)
    }

    override suspend fun deleteRuleLocationTimelessById(id: Int) {
        local.ruleLocationTimelessDao().deleteRuleLocationById(id)
    }

    override suspend fun getAllRuleLocationTimeless(): Flow<List<RuleLocationTimeless>> {
        return local.ruleLocationTimelessDao().getAllRuleLocations()
            .map { it.map { it.toRuleLocationTimeless() } }
    }
}
