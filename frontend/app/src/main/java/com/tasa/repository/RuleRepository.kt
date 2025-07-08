package com.tasa.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.Action
import com.tasa.domain.Alarm
import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.toTriggerTime
import com.tasa.geofence.GeofenceManager
import com.tasa.repository.interfaces.RuleRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.service.http.models.event.EventOutput
import com.tasa.service.http.models.location.LocationOutput
import com.tasa.service.http.models.rule.RuleEventOutput
import com.tasa.service.interfaces.ServiceWithRetry
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.localMode.RuleEventLocal
import com.tasa.storage.entities.localMode.RuleLocationLocal
import com.tasa.storage.entities.remote.AlarmRemote
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.NetworkChecker
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
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
    private val networkChecker: NetworkChecker,
    userRepo: UserRepository,
) : RuleRepositoryInterface, ServiceWithRetry(userRepo) {
    private suspend fun getToken(): String {
        return userInfoRepository.getToken() ?: throw AuthenticationException(
            "User is not authenticated. Please log in again.",
            null,
        )
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun getFromApi(): Either<ApiError, Unit> {
        return when (val result = retryOnFailure { remote.ruleService.fetchRules(getToken()) }) {
            is Success -> {
                val ruleEvents = result.value.eventRules
                // associate external event IDs with local events
                val events: Map<EventOutput, Event> =
                    ruleEvents.mapNotNull { it ->
                        val localEvent =
                            queryCalendarService.toLocalEvent(
                                it.event.id,
                                it.event.title,
                                it.event.startTime,
                                it.event.endTime,
                            )
                        if (localEvent != null) it.event to localEvent else null
                    }.toMap()
                val localEvents =
                    local.remoteDao().getAllEvents().map { it.toEvent() }
                // Update existing events and insert new ones
                val toUpdate: Map<EventOutput, Event> =
                    events.filter { (_, outEvent) ->
                        localEvents.any { localEvent ->
                            localEvent.eventId == outEvent.eventId &&
                                localEvent.calendarId == outEvent.calendarId
                        }
                    }
                toUpdate.forEach {
                        outEvent ->
                    local.remoteDao().updateEventRemote(
                        outEvent.key.id,
                        outEvent.value.eventId,
                        outEvent.value.calendarId,
                        outEvent.value.title,
                    )
                }
                val toInsert: Map<EventOutput, Event> =
                    events.filter { (_, outEvent) ->
                        localEvents.none { localEvent ->
                            localEvent.eventId == outEvent.eventId &&
                                localEvent.calendarId == outEvent.calendarId
                        }
                    }
                // Insert new events
                local.remoteDao().insertEventRemote(
                    *toInsert.map { event -> event.value.toEventRemote() }.toTypedArray(),
                )
                val localRuleEvents = local.remoteDao().getAllRuleEventsWithEventRemote().map { it.toRuleEvent() }
                // The rule events with a event that was successfully inserted or updated
                val ruleEventsFiltered =
                    ruleEvents.filter {
                        it.event in events.keys
                    }
                val ruleEventsToUpdate: Map<RuleEventOutput, RuleEvent> =
                    ruleEventsFiltered
                        .filter { ruleEvent -> localRuleEvents.any { it.id == ruleEvent.id } }
                        .associateWith { ruleEvent ->
                            localRuleEvents.first { it.id == ruleEvent.id }
                        }
                // alarms for rule events that are in the local database
                val startAlarms: Map<Alarm, RuleEvent> =
                    localRuleEvents.mapNotNull { ruleEvent ->
                        local.remoteDao().getAlarmByTriggerTime(ruleEvent.startTime.toTriggerTime().value)
                            ?.toAlarm()
                            ?.let { alarm -> alarm to ruleEvent }
                    }.toMap().filter { it.key.triggerTime != it.value.startTime.toTriggerTime().value }
                val endAlarms: Map<Alarm, RuleEvent> =
                    localRuleEvents.mapNotNull { ruleEvent ->
                        local.remoteDao().getAlarmByTriggerTime(ruleEvent.endTime.toTriggerTime().value)
                            ?.toAlarm()
                            ?.let { alarm -> alarm to ruleEvent }
                    }.toMap().filter { it.key.triggerTime != it.value.endTime.toTriggerTime().value }
                // Update rule events that are in the local database
                ruleEventsToUpdate.forEach { (key, value) ->
                    if (!isCollisionWithAnother(value, key.startTime, key.endTime)) {
                        local.remoteDao().updateRuleEventRemote(
                            key.id,
                            key.startTime,
                            key.endTime,
                        )
                    }
                }
                // External rule events that are not in the local database
                val rulesEventToInsert: Map<RuleEventOutput, RuleEvent> =
                    ruleEventsFiltered.filter { ruleEvent -> localRuleEvents.none { it.id == ruleEvent.id } }
                        .associateWith { ruleEvent ->
                            localRuleEvents.first { it.id == ruleEvent.id }
                        }
                // Insert rule events that are not in the local database
                local.remoteDao().insertRuleEventRemote(
                    *rulesEventToInsert.map { ruleEvent ->
                        ruleEvent.value.toRuleRemote()
                    }.toTypedArray(),
                )
                // Schedule alarms for new rule events
                rulesEventToInsert.keys.forEach {
                    val alarmIdStart =
                        local.remoteDao().insertAlarmRemote(
                            AlarmRemote(
                                triggerTime = it.startTime.toTriggerTime().value,
                                action = Action.MUTE,
                                ruleId = it.id,
                            ),
                        ).toInt()
                    ruleScheduler.scheduleAlarm(
                        alarmIdStart,
                        it.startTime
                            .toTriggerTime(),
                        Action.MUTE,
                    )
                    val alarmIdEnd =
                        local.remoteDao().insertAlarmRemote(
                            AlarmRemote(
                                triggerTime = it.endTime.toTriggerTime().value,
                                action = Action.UNMUTE,
                                ruleId = it.id,
                            ),
                        ).toInt()
                    ruleScheduler.scheduleAlarm(
                        alarmIdEnd,
                        it.endTime.toTriggerTime(),
                        Action.UNMUTE,
                    )
                }
                // update alarms for updated rule events
                startAlarms.forEach { (alarm, ruleEvent) ->
                    local.remoteDao().updateAlarmRemote(
                        time = ruleEvent.startTime.toTriggerTime().value,
                        id = alarm.id,
                    )
                }
                endAlarms.forEach { (alarm, ruleEvent) ->
                    local.remoteDao().updateAlarmRemote(
                        time = ruleEvent.endTime.toTriggerTime().value,
                        id = alarm.id,
                    )
                }
                val ruleLocations = result.value.locationRules
                val localLocations = local.remoteDao().getAllLocations().map { it.toLocation() }
                val outLocations = ruleLocations.map { it.location }
                val locationsToUpdate: Map<LocationOutput, Location> =
                    outLocations.mapNotNull { outLocation ->
                        localLocations.find { localLocation -> localLocation.id == outLocation.id }
                            ?.let { outLocation to it }
                    }.toMap()
                val locationsToInsert: Map<LocationOutput, Location> =
                    outLocations.filter { outLocation ->
                        localLocations.none { localLocation -> localLocation.id == outLocation.id }
                    }.associateWith { it.toLocation() }
                locationsToUpdate.forEach {
                    local.remoteDao().updateLocationRemote(
                        it.key.id,
                        it.key.name,
                        it.key.latitude,
                        it.key.longitude,
                        it.key.radius,
                    )
                }
                local.remoteDao().insertLocationRemote(
                    *locationsToInsert.map { it.value.toLocationRemote() }.toTypedArray(),
                )
                local.remoteDao().insertRuleLocationRemote(
                    *ruleLocations.map { ruleLocation ->
                        ruleLocation.toRuleLocationRemote()
                    }.toTypedArray(),
                )
                /*ruleLocations.forEach {
                    val existing =
                        local.remoteDao().getRuleLocationsByLocationNameResult(
                            it.location.name,
                        ) // TODO
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
                }*/
                success(Unit)
            }
            is Failure -> failure(result.value)
        }
    }

    private suspend fun hasRules(): Boolean {
        return if (userInfoRepository.isLocal()) {
            local.localDao().hasRuleEvents() || local.localDao().hasRuleLocations()
        } else {
            local.remoteDao().hasRuleEvents() || local.remoteDao().hasRuleLocations()
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun fetchAllRules(): Either<ApiError, Flow<List<Rule>>> {
        return if (userInfoRepository.isLocal()) {
            success(getFromLocal())
        } else {
            when (val result = getFromApi()) {
                is Success -> success(getFromLocal())
                is Failure -> failure(result.value)
            }
        }
    }

    private suspend fun getFromLocal(): Flow<List<Rule>> {
        return if (userInfoRepository.isLocal()) {
            val ruleEventFlow =
                local.localDao().getAllRuleEventsWithEventLocalFlow()
                    .map { it.map { it.toRuleEvent() } }.map {
                        it.sortedBy { it.startTime }
                    }.map { it.filter { rule -> rule.endTime.isAfter(LocalDateTime.now()) } }
            val ruleLocationFlow =
                local.localDao().getAllRuleLocationsWithLocationFlow().map { it.map { it.toRuleLocationTimeless() } }
            combine(
                ruleEventFlow,
                ruleLocationFlow,
            ) { events, locations ->
                (events + locations)
            }
        } else {
            val ruleEventFlow =
                local.remoteDao().getAllRuleEventsWithEventRemoteFlow().map { it.map { it.toRuleEvent() } }
                    .map {
                        it.sortedBy { it.startTime } 
                    }.map { it.filter { rule -> rule.endTime.isAfter(LocalDateTime.now()) } }
            val ruleLocationFlow =
                local.remoteDao().getAllRuleLocationsWithLocationFlow().map { it.map { it.toRuleLocationTimeless() } }
            combine(
                ruleEventFlow,
                ruleLocationFlow,
            ) { events, locations ->
                (events + locations)
            }
        }
    }

    override suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean {
        if (userInfoRepository.isLocal()) {
            val rules = local.localDao().getAllRuleEventsWithEventLocal().map { it.toRuleEvent() }
            return rules.any { it.startTime.isBefore(endTime) && it.endTime.isAfter(startTime) }
        } else {
            val rules = local.remoteDao().getAllRuleEventsWithEventRemote().map { it.toRuleEvent() }
            return rules.any { it.startTime.isBefore(endTime) && it.endTime.isAfter(startTime) }
        }
    }

    override suspend fun isCollisionWithAnother(
        rule: Rule,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
    ): Boolean {
        if (userInfoRepository.isLocal()) {
            val rules = local.localDao().getAllRuleEventsWithEventLocal().map { it.toRuleEvent() }
            return rules.any { it.id != rule.id && it.startTime.isBefore(newEndTime) && it.endTime.isAfter(newStartTime) }
        } else {
            val rules = local.remoteDao().getAllRuleEventsWithEventRemote().map { it.toRuleEvent() }
            return rules.any { it.id != rule.id && it.startTime.isBefore(newEndTime) && it.endTime.isAfter(newStartTime) }
        }
    }

    override suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): Either<ApiError, RuleEvent> {
        if (userInfoRepository.isLocal()) {
            val rule =
                RuleEventLocal(
                    startTime = startTime,
                    endTime = endTime,
                    externalId = event.id,
                )
            val id =
                local.localDao().insertRuleEventLocal(
                    rule,
                ).toInt()
            return success(
                RuleEvent(
                    id = id,
                    startTime = startTime,
                    endTime = endTime,
                    event = event,
                ),
            )
        } else {
            return when (
                val rule =
                    retryOnFailure {
                        remote.ruleService.insertRuleEvent(
                            startTime = startTime,
                            endTime = endTime,
                            event = event,
                            getToken(),
                        )
                    }
            ) {
                is Success -> {
                    val ruleEventEntity = rule.value.toRuleRemote()
                    local.remoteDao().insertRuleEventRemote(ruleEventEntity)
                    success(rule.value)
                }
                is Failure -> failure(rule.value)
            }
        }
    }

    override suspend fun deleteRuleEventById(id: Int) {
        if (userInfoRepository.isLocal()) {
            local.localDao().deleteEventLocalById(id)
            return
        } else {
            retryOnFailure { remote.ruleService.deleteRuleEventById(id, getToken()) }
        }
    }

    override suspend fun cleanOldRules(now: LocalDateTime) {
        if (userInfoRepository.isLocal()) {
            val rules = local.localDao().getAllRuleEventsWithEventLocal().map { it.toRuleEvent() }
            val oldRules = rules.filter { it.endTime.isBefore(now) }
            oldRules.forEach { rule ->
                local.localDao().deleteRuleEventLocalById(
                    rule.id,
                )
            }
        } else {
            val rules = local.remoteDao().getAllRuleEventsWithEventRemote().map { it.toRuleEvent() }
            val oldRules = rules.filter { it.endTime.isBefore(now) }
            oldRules.forEach { rule ->
                retryOnFailure { remote.ruleService.deleteRuleEventById(rule.id, getToken()) }
                local.remoteDao().deleteRuleEventRemoteById(rule.id)
            }
        }
    }

    override suspend fun clean() {
        if (userInfoRepository.isLocal()) {
            local.localDao().clearRuleEvents()
            local.localDao().clearRuleLocations()
        } else {
            local.remoteDao().clearRuleEvents()
            local.remoteDao().clearRuleLocations()
        }
    }

    override suspend fun updateRuleEvent(
        rule: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        oldStartTime: LocalDateTime,
        oldEndTime: LocalDateTime,
    ): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal()) {
            local.localDao().updateRuleEventLocal(
                id = rule.id,
                startTime = newStartTime,
                endTime = newEndTime,
            )
            return success(Unit)
        }
        val remote =
            retryOnFailure {
                remote.ruleService.updateRuleEvent(
                    rule,
                    newStartTime,
                    newEndTime,
                    getToken(),
                )
            }
        return when (remote) {
            is Success -> {
                local.remoteDao().updateRuleEventRemote(
                    rule.id,
                    newStartTime,
                    newEndTime,
                )
                return success(Unit)
            }
            is Failure -> return failure(remote.value)
        }
    }

    override suspend fun deleteRuleEvent(rule: RuleEvent): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal()) {
            local.localDao().deleteRuleEventLocalById(rule.id)
            return success(Unit)
        } else {
            val remote =
                retryOnFailure { remote.ruleService.deleteRuleEventById(rule.id, getToken()) }
            return when (remote) {
                is Success -> {
                    local.remoteDao().deleteRuleEventRemoteById(
                        rule.id,
                    )
                    success(Unit)
                }

                is Failure -> failure(remote.value)
            }
        }
    }

    override suspend fun getTimelessRulesForLocation(location: Location): List<RuleLocationTimeless> {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getAllRuleLocationsWithLocation().filter {
                it.location.name == location.name
            }.map { it.toRuleLocationTimeless() }
        } else {
            local.remoteDao().getAllRuleLocationsWithLocation().filter {
                it.location.name == location.name
            }.map { it.toRuleLocationTimeless() }
        }
    }

    override suspend fun insertRuleLocationTimeless(
        location: Location,
    ): Either<
        ApiError,
        RuleLocationTimeless,
        > {
        if (userInfoRepository.isLocal()) {
            val entity =
                RuleLocationLocal(
                    locationId = location.id,
                )
            val id =
                local.localDao().insertRuleLocationLocal(
                    entity,
                ).toInt()
            val rule =
                RuleLocationTimeless(
                    id = id,
                    location = location,
                )
            return success(rule)
        } else {
            val remote =
                retryOnFailure {
                    remote.ruleService.insertRuleLocation(
                        location,
                        getToken(),
                    )
                }
            when (remote) {
                is Success -> {
                    local.remoteDao().insertRuleLocationRemote(remote.value.toEntityRemote())
                    return success(remote.value)
                }
                is Failure -> return failure(remote.value)
            }
        }
    }

    override suspend fun deleteRuleLocationTimeless(ruleLocation: RuleLocationTimeless): Either<ApiError, RuleLocationTimeless> {
        if (userInfoRepository.isLocal()) {
            local.localDao().deleteRuleLocationLocalById(ruleLocation.id)
            return success(ruleLocation)
        }
        val remote = retryOnFailure { remote.ruleService.deleteRuleLocationById(ruleLocation.id, getToken()) }
        return when (remote) {
            is Success -> {
                local.remoteDao().deleteRuleLocationRemoteById(ruleLocation.id)
                success(ruleLocation)
            }
            is Failure -> failure(remote.value)
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    override suspend fun syncRules(): Either<ApiError, Unit> {
        return if (userInfoRepository.isLocal()) {
            success(Unit)
        } else {
            when (val result = getFromApi()) {
                is Success -> {
                    success(Unit)
                }
                is Failure -> failure(result.value)
            }
        }
    }
}
