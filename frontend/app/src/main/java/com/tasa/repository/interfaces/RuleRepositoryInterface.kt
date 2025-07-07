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

    suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): Either<ApiError, RuleEvent>

    suspend fun deleteRuleEventById(id: Int)

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
        rule: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        oldStartTime: LocalDateTime,
        oldEndTime: LocalDateTime,
    ): Either<ApiError, Unit>

    suspend fun deleteRuleEvent(rule: RuleEvent): Either<ApiError, Unit>

    suspend fun getTimelessRulesForLocation(location: Location): List<RuleLocationTimeless>

    suspend fun insertRuleLocationTimeless(location: Location): Either<ApiError, RuleLocationTimeless>

    suspend fun deleteRuleLocationTimeless(ruleLocation: RuleLocationTimeless): Either<ApiError, RuleLocationTimeless>

    suspend fun syncRules(): Either<ApiError, Unit>
}
