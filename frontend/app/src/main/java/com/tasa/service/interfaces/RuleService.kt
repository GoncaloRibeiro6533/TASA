package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocation
import com.tasa.utils.Either
import java.time.LocalDateTime

interface RuleService {
    suspend fun fetchRules(token: String): Either<ApiError, List<Rule>>

    suspend fun fetchRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleEvent>

    suspend fun fetchRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleLocation>

/*    suspend fun fetchRulesByTime(
        startTime: Long,
        endTime: Long,
    ): Either<ApiError, List<Rule>>
*/
    suspend fun insertRuleEvent(
        ruleEvent: RuleEvent,
        token: String,
    ): Either<ApiError, RuleEvent>

    suspend fun insertRuleLocation(
        ruleLocation: RuleLocation,
        token: String,
    ): Either<ApiError, RuleLocation>

    suspend fun deleteRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>

    suspend fun deleteRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>

    suspend fun updateRuleEvent(
        ruleEvent: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        token: String,
    ): Either<ApiError, RuleEvent>

    suspend fun updateRuleLocation(
        ruleLocation: RuleLocation,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        token: String,
    ): Either<ApiError, RuleLocation>
}
