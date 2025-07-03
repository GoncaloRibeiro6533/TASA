package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocationTimeless
import com.tasa.service.http.models.rule.RuleEventOutput
import com.tasa.service.http.models.rule.RuleListOutput
import com.tasa.utils.Either
import java.time.LocalDateTime

interface RuleService {
    suspend fun fetchRules(token: String): Either<ApiError, RuleListOutput>

    suspend fun fetchRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleEventOutput>

    suspend fun fetchRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleLocationTimeless>

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
        ruleLocation: RuleLocationTimeless,
        token: String,
    ): Either<ApiError, RuleLocationTimeless>

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
}
