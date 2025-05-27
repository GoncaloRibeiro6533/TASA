package com.tasa.repository

import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.repository.interfaces.RuleRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class RuleRepository(
    private val local: TasaDB,
    private val remote: TasaService,
) : RuleRepositoryInterface {
    override suspend fun fetchAllRules(): Flow<List<Rule>> {
        val ruleEvent = local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } }
        val ruleLocation =
            local.ruleLocationDao().getAllRuleLocations().map { it.map { it.toRuleLocation() } }
        return ruleEvent.combine(ruleLocation) { events, locations ->
            events + locations
        }.map { it.filter { !it.endTime.isBefore(LocalDateTime.now()) } }
    }

    override suspend fun fetchRuleEvents(): Flow<List<RuleEvent>> {
        return local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } }
    }

    override suspend fun fetchRuleLocations(): Flow<List<RuleLocation>> {
        return local.ruleLocationDao().getAllRuleLocations().map { it.map { it.toRuleLocation() } }
    }

    override suspend fun fetchRuleEventsById(id: Int): RuleEvent? {
        TODO("Not yet implemented")
    }

    override suspend fun fetchRuleLocationsById(id: Int): RuleLocation? {
        TODO("Not yet implemented")
    }

    override suspend fun fetchRuleLocationsByName(name: String): RuleLocation? {
        TODO("Not yet implemented")
    }

    override suspend fun fetchRuleEventsCalendarIdAndEventId(
        calendarId: LocalDateTime,
        eventId: LocalDateTime,
    ): RuleEvent? {
        TODO("Not yet implemented")
    }

    override suspend fun fetchRuleByTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Rule? {
        val ruleEvent = local.ruleEventDao().getRuleEventByStartAndEndTime(startTime, endTime)
        val ruleLocation = local.ruleLocationDao().getRuleLocationByTime(startTime, endTime)
        if (ruleEvent != null) {
            return ruleEvent.toRuleEvent()
        }
        if (ruleLocation != null) {
            return ruleLocation.toRuleLocation()
        }
        return null
    }

    override suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean {
        val ruleEvent = local.ruleEventDao().getRuleEventByStartAndEndTime(startTime, endTime)
        val ruleLocation = local.ruleLocationDao().getRuleLocationByTime(startTime, endTime)
        return ruleEvent != null || ruleLocation != null
    }

    override suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): RuleEvent {
        val ruleEvent =
            RuleEvent(
                startTime = startTime,
                endTime = endTime,
                event = event,
            )
        local.eventDao().insertEvents(event.toEventEntity())
        local.ruleEventDao().insertRuleEvent(ruleEvent.toRuleEventEntity())
        return ruleEvent
    }

    override suspend fun insertRuleLocation(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        location: Location,
    ): RuleLocation {
        val ruleLocation =
            RuleLocation(
                startTime = startTime,
                endTime = endTime,
                location = location,
            )
        local.ruleLocationDao().insertRuleLocation(ruleLocation.toRuleLocationEntity())
        return ruleLocation
    }

    override suspend fun insertRuleEvents(ruleEvents: List<RuleEvent>) {
        local.ruleEventDao().insertRuleEvents(ruleEvents.map { it.toRuleEventEntity() })
    }

    override suspend fun insertRuleLocations(ruleLocations: List<RuleLocation>) {
        local.ruleLocationDao().insertRuleLocations(ruleLocations.map { it.toRuleLocationEntity() })
    }

    override suspend fun deleteRuleEventById(id: Int) {
        local.ruleEventDao().deleteRuleEventById(id)
    }

    override suspend fun deleteRuleLocationById(id: Int) {
        local.ruleLocationDao().deleteRuleLocationById(id)
    }

    override suspend fun deleteRuleLocationByName(name: String) {
        local.ruleLocationDao().deleteRuleLocationByName(name)
    }

    override suspend fun deleteRuleEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ) {
        local.ruleEventDao().deleteRuleEventByEventIdAndCalendarId(eventId, calendarId)
    }

    override suspend fun cleanOldRules(now: LocalDateTime) {
        local.ruleEventDao().getAllRuleEvents().map { it.map { it.toRuleEvent() } }.collect { rules ->
            val rulesToDelete = rules.filter { it.endTime.isBefore(now) }
            val events = rulesToDelete.map { it.event }
            val eventsNotToDelete = rules.filter { it !in rulesToDelete && it.event in events }.map { it.event }
            events.filter { it !in eventsNotToDelete }.forEach {
                local.eventDao().deleteEvent(it.id, it.calendarId)
            }
            rulesToDelete.forEach { local.ruleEventDao().deleteRuleEventByStartAndEndTime(it.startTime, it.endTime) }
            return@collect
        }
    }

    override suspend fun clean() {
        local.ruleEventDao().clear()
        local.ruleLocationDao().clear()
    }
}
