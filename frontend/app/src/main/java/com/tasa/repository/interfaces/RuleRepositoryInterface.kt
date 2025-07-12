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

/**
 * RuleRepositoryInterface defines the contract for managing rules in the application.
 * It provides methods to fetch, insert, delete, and update rules, as well as synchronize them with a remote source.
 */
interface RuleRepositoryInterface {
    /**
     * Fetches a flow of rules.
     * @return Either an ApiError or a Flow of List of Rule.
     */
    suspend fun fetchAllRules(): Either<ApiError, Flow<List<Rule>>>

    /**
     * Inserts a new rule with the specified parameters.
     * @param startTime The start time of the rule.
     * @param endTime The end time of the rule.
     * @param event The event associated with the rule.
     * @return Either an ApiError or the inserted Rule.
     */
    suspend fun insertRuleEvent(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        event: Event,
    ): Either<ApiError, RuleEvent>

    /**
     * Deletes a rule event by its ID.
     * @param id The ID of the rule event to be deleted.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteRuleEventById(id: Int)

    /**
     * Checks if there is a collision with any existing rule for the specified time range.
     * @param startTime The start time of the rule to check for collision.
     * @param endTime The end time of the rule to check for collision.
     * @return Boolean indicating whether there is a collision with any existing rule.
     */
    suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean

    /**
     * Checks if there is a collision with another rule for the specified time range.
     * @param rule The rule to check for collision.
     * @param newStartTime The new start time of the rule to check for collision.
     * @param newEndTime The new end time of the rule to check for collision.
     * @return Boolean indicating whether there is a collision with another rule.
     */
    suspend fun isCollisionWithAnother(
        rule: Rule,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
    ): Boolean

    /**
     * Deletes old rules that are no longer valid based on the current time.
     */
    suspend fun cleanOldRules(now: LocalDateTime)

    /**
     * Clears all rules from the repository.
     * @return Unit if successful.
     */
    suspend fun clean()

    /**
     * Updates an existing rule event with new start and end times.
     * @param rule The rule event to be updated.
     * @param newStartTime The new start time for the rule event.
     * @param newEndTime The new end time for the rule event.
     * @param oldStartTime The old start time of the rule event.
     * @param oldEndTime The old end time of the rule event.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun updateRuleEvent(
        rule: RuleEvent,
        newStartTime: LocalDateTime,
        newEndTime: LocalDateTime,
        oldStartTime: LocalDateTime,
        oldEndTime: LocalDateTime,
    ): Either<ApiError, Unit>

    /**
     * Deletes a rule event.
     * @param rule The rule event to be deleted.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteRuleEvent(rule: RuleEvent): Either<ApiError, Unit>

    /**
     * Retrieves a rule event by location.
     * @param location The location for which the rule is to be retrieved.
     * @return Either an ApiError or the RuleEvent if found.
     */
    suspend fun getTimelessRulesForLocation(location: Location): List<RuleLocationTimeless>

    /**
     * Inserts a new timeless rule location.
     * @param location The location for which the timeless rule is to be created.
     * @return Either an ApiError or the inserted RuleLocationTimeless.
     */
    suspend fun insertRuleLocationTimeless(location: Location): Either<ApiError, RuleLocationTimeless>

    /**
     * Deletes an existing timeless rule location.
     * @param ruleLocation The rule location to be deleted.
     * @return Either an ApiError or the deleted RuleLocationTimeless.
     */
    suspend fun deleteRuleLocationTimeless(ruleLocation: RuleLocationTimeless): Either<ApiError, RuleLocationTimeless>

    /**
     * Synchronizes rules with a remote source.
     * @return Either an ApiError or the Unit if successful.
     */
    suspend fun syncRules(): Either<ApiError, Unit>
}
