package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.domain.Location
import com.tasa.domain.RuleEvent
import com.tasa.domain.RuleLocationTimeless
import com.tasa.service.http.models.rule.RuleEventOutput
import com.tasa.service.http.models.rule.RuleListOutput
import com.tasa.utils.Either
import java.time.LocalDateTime

/**
 * RuleService defines the contract for managing rules in the application.
 * It provides methods to fetch, insert, delete, and update rules.
 */
interface RuleService {
    /**
     * Fetches all rules.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the list of RuleListOutput.
     */
    suspend fun fetchRules(token: String): Either<ApiError, RuleListOutput>

    /**
     * Fetches a rule event by its ID.
     * @param id The ID of the rule event to be retrieved.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the retrieved RuleEventOutput.
     */
    suspend fun fetchRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleEventOutput>

    /**
     * Fetches a rule location by its ID.
     * @param id The ID of the rule location to be retrieved.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the retrieved RuleLocationTimeless.
     */
    suspend fun fetchRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, RuleLocationTimeless>

    /**
     * Inserts a new rule event with the specified parameters.
     * @param startTime The start time of the rule event.
     * @param endTime The end time of the rule event.
     * @param event The event associated with the rule.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the inserted RuleEvent.
     */
    suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
        token: String,
    ): Either<ApiError, RuleEvent>

    /**
     * Inserts a new rule location with the specified parameters.
     * @param location The location associated with the rule.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the inserted RuleLocationTimeless.
     */
    suspend fun insertRuleLocation(
        location: Location,
        token: String,
    ): Either<ApiError, RuleLocationTimeless>

    /**
     * Deletes a rule event by its ID.
     * @param id The ID of the rule event to be deleted.
     * @param token The authentication token for the request.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteRuleEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>

    /**
     * Deletes a rule location by its ID.
     * @param id The ID of the rule location to be deleted.
     * @param token The authentication token for the request.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteRuleLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>

    /**
     * Updates an existing rule event.
     * @param ruleEvent The rule event to be updated.
     * @param newStartTime The new start time of the rule event.
     * @param newEndTime The new end time of the rule event.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the updated RuleEvent.
     */
    suspend fun updateRuleEvent(
        ruleEvent: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        token: String,
    ): Either<ApiError, RuleEvent>
}
