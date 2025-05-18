package com.tasa.repository.interfaces

import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import kotlinx.coroutines.flow.Flow

interface RuleRepositoryInterface {
    suspend fun fetchRuleEvents(): Flow<List<RuleEvent>>

    suspend fun fetchRuleLocations(): List<RuleLocation>

    suspend fun fetchRuleEventsById(id: Int): RuleEvent?

    suspend fun fetchRuleLocationsById(id: Int): RuleLocation?

    suspend fun fetchRuleLocationsByName(name: String): RuleLocation?

    suspend fun fetchRuleEventsCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): RuleEvent?

    suspend fun fetchRuleByTime(
        startTime: Long,
        endTime: Long,
    ): Rule

    suspend fun insertRuleEvent(ruleEvent: RuleEvent)

    suspend fun insertRuleLocation(ruleLocation: RuleLocation)

    suspend fun insertRuleEvents(ruleEvents: List<RuleEvent>)

    suspend fun insertRuleLocations(ruleLocations: List<RuleLocation>)

    suspend fun deleteRuleEventById(id: Int)

    suspend fun deleteRuleLocationById(id: Int)

    suspend fun deleteRuleEventByName(name: String)

    suspend fun deleteRuleLocationByName(name: String)

    suspend fun deleteRuleEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    )
}
