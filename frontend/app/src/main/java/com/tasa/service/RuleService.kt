package com.tasa.service

import com.tasa.domain.ApiError
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.utils.Either

interface RuleService {
    suspend fun fetchRules(): Either<ApiError, List<Rule>>

    suspend fun fetchRuleEventById(id: Int): Either<ApiError, RuleEvent>

    suspend fun fetchRulesEventByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Either<ApiError, List<RuleEvent>>

    suspend fun fetchRuleLocationById(id: Int): Either<ApiError, RuleLocation>

    suspend fun fetchRulesLocationByName(name: String): Either<ApiError, List<RuleLocation>>

    suspend fun fetchRulesByTime(
        startTime: Long,
        endTime: Long,
    ): Either<ApiError, List<Rule>>

    suspend fun insertRuleEvent(ruleEvent: RuleEvent): Either<ApiError, RuleEvent>

    suspend fun insertRuleLocation(ruleLocation: RuleLocation): Either<ApiError, RuleLocation>

    suspend fun insertRuleEvents(ruleEvents: List<RuleEvent>): Either<ApiError, List<RuleEvent>>

    suspend fun insertRuleLocations(ruleLocations: List<RuleLocation>): Either<ApiError, List<RuleLocation>>

    suspend fun deleteRuleEventById(id: Int): Either<ApiError, Unit>

    suspend fun deleteRuleLocationById(id: Int): Either<ApiError, Unit>
}
