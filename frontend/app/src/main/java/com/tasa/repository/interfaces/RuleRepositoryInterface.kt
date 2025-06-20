package com.tasa.repository.interfaces

import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface RuleRepositoryInterface {
    suspend fun fetchAllRules(): Flow<List<Rule>>

    suspend fun fetchRuleEvents(): Flow<List<RuleEvent>>

    suspend fun fetchRuleLocations(): Flow<List<RuleLocation>>

    suspend fun fetchRuleEventsById(id: Int): RuleEvent?

    suspend fun fetchRuleLocationsById(id: Int): RuleLocation?

    suspend fun fetchRuleLocationsByName(name: String): List<RuleLocation>

    suspend fun fetchRuleEventsCalendarIdAndEventId(
        calendarId: LocalDateTime,
        eventId: LocalDateTime,
    ): RuleEvent?

    suspend fun fetchRuleByTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Rule?

    suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): RuleEvent

    suspend fun insertRuleLocation(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        location: Location,
        geofenceId: Int,
    ): RuleLocation

    suspend fun insertRuleEvents(ruleEvents: List<RuleEvent>)

    suspend fun insertRuleLocations(ruleLocations: Map<RuleLocation, Int>)

    suspend fun deleteRuleEventById(id: Int)

    suspend fun deleteRuleLocationById(id: Int)

    suspend fun deleteRuleEventByCalendarIdAndEventId(
        eventId: Long,
        calendarId: Long,
    )

    suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean

    suspend fun deleteRuleLocationByName(name: String)

    suspend fun cleanOldRules(now: LocalDateTime)

    suspend fun clean()

    suspend fun updateRuleEvent(
        id: Int? = null,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        oldStartTime: LocalDateTime,
        oldEndTime: LocalDateTime,
    )

    suspend fun deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndtime(
        eventId: Long,
        calendarId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    )

    suspend fun getRuleLocationByGeofenceId(geofenceId: Int): List<RuleLocation>
}
