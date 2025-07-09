package com.tasa.repository

import android.Manifest
import android.util.Log
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
import com.tasa.location.LocationService
import com.tasa.repository.interfaces.RuleRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.service.http.models.event.EventOutput
import com.tasa.service.http.models.location.LocationOutput
import com.tasa.service.http.models.rule.RuleEventOutput
import com.tasa.service.http.models.rule.RuleLocationOutput
import com.tasa.service.interfaces.ServiceWithRetry
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.localMode.RuleEventLocal
import com.tasa.storage.entities.localMode.RuleLocationLocal
import com.tasa.storage.entities.remote.AlarmRemote
import com.tasa.storage.entities.remote.GeofenceRemote
import com.tasa.storage.entities.remote.RuleLocationRemoteWithLocation
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.NetworkChecker
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.ServiceKiller
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
    private val networkChecker: NetworkChecker,
    private val serviceKiller: ServiceKiller,
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
        return when (val result = retryOnFailure { token -> remote.ruleService.fetchRules(token) }) {
            is Success -> {
                val ruleEvents = result.value.eventRules
                // associate external event IDs with local events
                val events: Map<EventOutput, Event> =
                    mapToLocalEvent(ruleEvents)
                val localEvents =
                    local.remoteDao().getAllEvents().map { it.toEvent() }
                // Update existing events and insert new ones
                val toUpdate: Map<EventOutput, Event> = eventsToUpdate(events, localEvents)
                toUpdate.forEach {
                        outEvent ->
                    local.remoteDao().updateEventRemote(
                        outEvent.key.id,
                        outEvent.value.eventId,
                        outEvent.value.calendarId,
                        outEvent.value.title,
                    )
                }
                val toInsert: Map<EventOutput, Event> = eventsToInsert(events, localEvents)
                // Insert new events
                local.remoteDao().insertEventRemote(
                    *toInsert.map { event -> event.value.toEventRemote() }.toTypedArray(),
                )
                val localRuleEvents =
                    local.remoteDao().getAllRuleEventsWithEventRemote().map { it.toRuleEvent() }
                // The rule events with a event that was successfully inserted or updated
                val ruleEventsFiltered =
                    ruleEvents.filter {
                        it.event.id in events.keys.map { it.id }
                    }
                val ruleEventsToUpdate: Map<RuleEventOutput, RuleEvent> =
                    getRuleEventsToUpdate(ruleEventsFiltered, localRuleEvents)
                // alarms for rule events that are in the local database
                val startAlarms: Map<Alarm, RuleEvent> = startAlarmsToUpdate(localRuleEvents)
                val endAlarms: Map<Alarm, RuleEvent> = endAlarmsToUpdate(localRuleEvents)
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
                Log.d("RuleRepository", events.toString())
                // External rule events that are not in the local database
                val rulesEventToInsert: Map<RuleEventOutput, Event> =
                    getRulesEventToInsert(ruleEventsFiltered.filter { it.id !in localRuleEvents.map { it.id } }, events)
                // Insert rule events that are not in the local database
                local.remoteDao().insertRuleEventRemote(
                    *rulesEventToInsert.keys.map { ruleEvent ->
                        ruleEvent.toRuleEventRemote()
                    }.toTypedArray(),
                )
                // Schedule alarms for new rule events
                scheduleAlarmsForNewRules(rulesEventToInsert.keys)
                // update alarms for updated rule events
                rescheduleAlarms(startAlarms, endAlarms)
                val ruleLocations = result.value.locationRules
                val localLocations = local.remoteDao().getAllLocations().map { it.toLocation() }
                val outLocations = ruleLocations.map { it.location }
                val locationsToUpdate: Map<LocationOutput, Location> =
                    getLocationsToUpdate(outLocations, localLocations)
                val locationUpdateMapById = locationsToUpdate.keys.associateBy { it.id }
                val ruleLocationsWithLocationToUpdate: Map<RuleLocationRemoteWithLocation, LocationOutput> =
                    getRuleLocationsWithLocationsToUpdate(locationUpdateMapById)
                val locationsToInsert: Map<LocationOutput, Location> =
                    getLocationsToInsert(outLocations, localLocations)
                locationsToUpdate.forEach {
                    local.remoteDao().updateLocationRemote(
                        it.key.id,
                        it.key.name,
                        it.key.latitude,
                        it.key.longitude,
                        it.key.radius,
                    )
                }
                val geofencesToUpdate: Map<GeofenceRemote, Pair<LocationOutput, Location>> =
                    getGeofencesToUpdate(ruleLocationsWithLocationToUpdate)
                updateGeofences(geofencesToUpdate)
                local.remoteDao().insertLocationRemote(
                    *locationsToInsert.map { it.value.toLocationRemote() }.toTypedArray(),
                )
                val ruleLocationsIds = ruleLocations.map { it.id }.toSet()
                val ruleLocationsToUpdate: Map<RuleLocationRemoteWithLocation, RuleLocationTimeless> =
                    getRuleLocationsToUpdate(ruleLocationsIds)
                val ruleLocationsToUpdateIds = ruleLocationsToUpdate.keys.map { it.ruleLocation.id }
                val ruleLocationsToInsert: Map<RuleLocationOutput, RuleLocationTimeless> =
                    ruleLocations.filter { it.id !in ruleLocationsToUpdateIds }
                        .associateWith { it.toRuleLocationTimeless() }
                local.remoteDao().insertRuleLocationRemote(
                    *ruleLocationsToInsert.keys.map { ruleLocation ->
                        ruleLocation.toRuleLocationRemote()
                    }.toTypedArray(),
                )
                registerGeofenceForNewLocationRules(ruleLocationsToInsert)

                success(Unit)
            }
            is Failure -> failure(result.value)
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun updateGeofences(geofencesToUpdate: Map<GeofenceRemote, Pair<LocationOutput, Location>>) {
        geofencesToUpdate.forEach { (geofence, pair) ->
            val (locationOutput, location) = pair
            val locationInService = LocationService
            if (!locationInService.isRunning || locationInService.locationName
                != location.name
            ) {
                val radius =
                    if (locationOutput.radius < 100) {
                        100f
                    } else {
                        locationOutput.radius.toFloat()
                    }
                geofenceManager.deregisterGeofence(geofence.name)
                local.remoteDao().updateGeofenceRemote(
                    id = geofence.id,
                    latitude = locationOutput.latitude,
                    longitude = locationOutput.longitude,
                    radius = radius.toDouble(),
                    name = locationOutput.name,
                )
                geofenceManager.registerGeofence(
                    key = locationOutput.name,
                    location = locationOutput.toLocation().toLocation(),
                    radiusInMeters = radius,
                )
            } else {
                if (serviceKiller.killServices(LocationService::class) == Unit) {
                    geofenceManager.deregisterGeofence(geofence.name)
                    local.remoteDao().updateGeofenceRemote(
                        id = geofence.id,
                        latitude = locationOutput.latitude,
                        longitude = locationOutput.longitude,
                        radius = locationOutput.radius.toDouble(),
                        name = locationOutput.name,
                    )
                    geofenceManager.registerGeofence(
                        key = locationOutput.name,
                        location = locationOutput.toLocation().toLocation(),
                        radiusInMeters = locationOutput.radius.toFloat(),
                    )
                }
            }
        }
    }

    private suspend fun getGeofencesToUpdate(
        ruleLocationsWithLocationToUpdate: Map<RuleLocationRemoteWithLocation, LocationOutput>,
    ): Map<GeofenceRemote, Pair<LocationOutput, Location>> {
        val map: Map<GeofenceRemote, Pair<LocationOutput, Location>> =
            local.remoteDao().getAllGeofences()
                .filter { geofence ->
                    ruleLocationsWithLocationToUpdate.keys
                        .any { ruleLoc -> ruleLoc.ruleLocation.id == geofence.ruleId }
                }
                .associateWith { geofence ->
                    val (rule, location) =
                        ruleLocationsWithLocationToUpdate.entries
                            .first { it -> it.key.ruleLocation.id == geofence.ruleId }
                    location to rule.location.toLocation()
                }
        return map.filter {
            it.value.first.name != it.key.name ||
                it.value.first.latitude != it.key.latitude ||
                it.value.first.longitude != it.key.longitude ||
                it.value.first.radius != it.key.radius
        } // Check if radius has changed
    }

    private suspend fun getRuleLocationsWithLocationsToUpdate(
        locationUpdateMapById: Map<Int, LocationOutput>,
    ): Map<RuleLocationRemoteWithLocation, LocationOutput> =
        local.remoteDao()
            .getAllRuleLocationsWithLocation()
            .mapNotNull { ruleLoc ->
                locationUpdateMapById[ruleLoc.location.id]?.let { updatedLoc ->
                    ruleLoc to updatedLoc
                }
            }.toMap()

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private suspend fun registerGeofenceForNewLocationRules(ruleLocationsToInsert: Map<RuleLocationOutput, RuleLocationTimeless>) {
        ruleLocationsToInsert.forEach { (ruleLocation, ruleLocationTimeless) ->
            val radius =
                if (ruleLocation.location.radius < 100) {
                    100f
                } else {
                    ruleLocation.location.radius.toFloat()
                }
            geofenceManager.registerGeofence(
                key = ruleLocation.location.name,
                location = ruleLocationTimeless.location.toLocation(),
                radiusInMeters = radius,
            )
            local.remoteDao().insertGeofenceRemote(
                GeofenceRemote(
                    latitude = ruleLocationTimeless.location.latitude,
                    longitude = ruleLocationTimeless.location.longitude,
                    radius = radius.toDouble(),
                    ruleId = ruleLocation.id,
                    name = ruleLocationTimeless.location.name,
                ),
            )
        }
    }

    private suspend fun getRuleLocationsToUpdate(ruleLocationsIds: Set<Int>): Map<RuleLocationRemoteWithLocation, RuleLocationTimeless> =
        local.remoteDao().getAllRuleLocationsWithLocation()
            .filter { it.ruleLocation.id in ruleLocationsIds }
            .associateWith { it.toRuleLocationTimeless() }

    private fun getLocationsToInsert(
        outLocations: List<LocationOutput>,
        localLocations: List<Location>,
    ): Map<LocationOutput, Location> =
        outLocations.filter { outLocation ->
            localLocations.none { localLocation -> localLocation.id == outLocation.id }
        }.associateWith { it.toLocation() }

    private fun getLocationsToUpdate(
        outLocations: List<LocationOutput>,
        localLocations: List<Location>,
    ): Map<LocationOutput, Location> =
        outLocations.mapNotNull { outLocation ->
            localLocations.find { localLocation ->
                localLocation.id == outLocation.id &&
                    (
                        localLocation.name != outLocation.name ||
                            localLocation.latitude != outLocation.latitude ||
                            localLocation.longitude != outLocation.longitude ||
                            localLocation.radius != outLocation.radius
                    )
            }
                ?.let { outLocation to it }
        }.toMap()

    private suspend fun rescheduleAlarms(
        startAlarms: Map<Alarm, RuleEvent>,
        endAlarms: Map<Alarm, RuleEvent>,
    ) {
        startAlarms.forEach { (alarm, ruleEvent) ->
            local.remoteDao().updateAlarmRemote(
                time = ruleEvent.startTime.toTriggerTime().value,
                id = alarm.id,
            )
            ruleScheduler.updateAlarm(
                alarm.id,
                ruleEvent.startTime.toTriggerTime(),
                Action.MUTE,
            )
        }
        endAlarms.forEach { (alarm, ruleEvent) ->
            local.remoteDao().updateAlarmRemote(
                time = ruleEvent.endTime.toTriggerTime().value,
                id = alarm.id,
            )
        }
    }

    private suspend fun scheduleAlarmsForNewRules(rulesEventToInsert: Set<RuleEventOutput>) {
        rulesEventToInsert.forEach {
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
    }

    private fun getRulesEventToInsert(
        ruleEventsFiltered: List<RuleEventOutput>,
        events: Map<EventOutput, Event>,
    ): Map<RuleEventOutput, Event> =
        ruleEventsFiltered.filter { ruleEvent ->
            events.any { it.key.id == ruleEvent.event.id }
        }.associateWith { ruleEvent ->
            events.getValue(ruleEvent.event)
        }

    private suspend fun endAlarmsToUpdate(localRuleEvents: List<RuleEvent>): Map<Alarm, RuleEvent> =
        localRuleEvents.mapNotNull { ruleEvent ->
            local.remoteDao().getAlarmsByRuleId(ruleEvent.id)
                .firstOrNull { it.action == Action.UNMUTE }
                ?.toAlarm()
                ?.let { alarm -> alarm to ruleEvent }
        }.toMap().filter { it.key.triggerTime != it.value.endTime.toTriggerTime().value }

    private suspend fun startAlarmsToUpdate(localRuleEvents: List<RuleEvent>): Map<Alarm, RuleEvent> =
        localRuleEvents.mapNotNull { ruleEvent ->
            local.remoteDao().getAlarmsByRuleId(ruleEvent.id)
                .firstOrNull { it.action == Action.MUTE }
                ?.toAlarm()
                ?.let { alarm -> alarm to ruleEvent }
        }.toMap().filter { it.key.triggerTime != it.value.startTime.toTriggerTime().value }

    private fun getRuleEventsToUpdate(
        ruleEventsFiltered: List<RuleEventOutput>,
        localRuleEvents: List<RuleEvent>,
    ): Map<RuleEventOutput, RuleEvent> =
        ruleEventsFiltered
            .filter { ruleEvent ->
                localRuleEvents.any {
                    it.id == ruleEvent.id &&
                        (it.startTime != ruleEvent.startTime || it.endTime != ruleEvent.endTime)
                }
            }
            .associateWith { ruleEvent ->
                localRuleEvents.first { it.id == ruleEvent.id }
            }

    private fun eventsToInsert(
        events: Map<EventOutput, Event>,
        localEvents: List<Event>,
    ): Map<EventOutput, Event> =
        events.filter { (_, outEvent) ->
            localEvents.none { localEvent ->
                localEvent.id == outEvent.id
            }
        }

    private fun eventsToUpdate(
        events: Map<EventOutput, Event>,
        localEvents: List<Event>,
    ): Map<EventOutput, Event> =
        events.filter { (_, outEvent) ->
            localEvents.any { localEvent ->
                localEvent.id == outEvent.id &&
                    localEvent.title != outEvent.title
            }
        }

    private fun mapToLocalEvent(ruleEvents: List<RuleEventOutput>): Map<EventOutput, Event> {
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
        return events
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
                    retryOnFailure { token ->
                        remote.ruleService.insertRuleEvent(
                            startTime = startTime,
                            endTime = endTime,
                            event = event,
                            token,
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
            retryOnFailure { token -> remote.ruleService.deleteRuleEventById(id, token) }
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
                retryOnFailure { token -> remote.ruleService.deleteRuleEventById(rule.id, token) }
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
            retryOnFailure { token ->
                remote.ruleService.updateRuleEvent(
                    rule,
                    newStartTime,
                    newEndTime,
                    token,
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
                retryOnFailure {
                        token ->
                    remote.ruleService.deleteRuleEventById(rule.id, token)
                }
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
                retryOnFailure { token ->
                    remote.ruleService.insertRuleLocation(
                        location,
                        token,
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
        val remote =
            retryOnFailure {
                    token ->
                remote.ruleService.deleteRuleLocationById(ruleLocation.id, token)
            }
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
        return if (userInfoRepository.isLocal() || userInfoRepository.getToken() == null) {
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
