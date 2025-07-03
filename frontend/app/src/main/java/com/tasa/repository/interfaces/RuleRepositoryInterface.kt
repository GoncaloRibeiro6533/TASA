package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocationTimeless
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface RuleRepositoryInterface {
    suspend fun fetchAllRules(): Either<ApiError, Flow<List<Rule>>>

    suspend fun fetchRuleEvents(): Flow<List<RuleEvent>>

    suspend fun fetchRuleLocations(): Flow<List<RuleLocationTimeless>>

    suspend fun fetchRuleLocationsByName(name: String): List<RuleLocationTimeless>

    suspend fun fetchRuleByTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Rule?

    suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): RuleEvent

    suspend fun deleteRuleEventById(id: Int)

    suspend fun deleteRuleEventByCalendarIdAndEventId(
        eventId: Long,
        calendarId: Long,
    )

    suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean

    suspend fun isCollisionWithAnother(
        rule: Rule,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
    ): Boolean

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

    suspend fun getTimelessRulesForLocation(location: Location): List<RuleLocationTimeless>

    suspend fun insertRuleLocationTimeless(
        location: Location,
        geofenceId: Int,
    ): RuleLocationTimeless

    suspend fun getRuleLocationTimelessById(id: Int): RuleLocationTimeless?

    suspend fun deleteRuleLocationTimelessByLocation(location: Location)

    suspend fun deleteRuleLocationTimelessById(id: Int)

    suspend fun getAllRuleLocationTimeless(): Flow<List<RuleLocationTimeless>>
}
