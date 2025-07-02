package pt.isel.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.AuthenticatedUser
import pt.isel.Either
import pt.isel.Failure
import pt.isel.RuleError
import pt.isel.RuleEvent
import pt.isel.RuleLocation
import pt.isel.RuleService
import pt.isel.Success
import pt.isel.errorHandlers.RuleErrorHandler
import pt.isel.models.rule.RuleEventInput
import pt.isel.models.rule.RuleEventOutput
import pt.isel.models.rule.RuleEventUpdateInput
import pt.isel.models.rule.RuleListOutput
import pt.isel.models.rule.RuleLocationInput
import pt.isel.models.rule.RuleLocationOutput
import pt.isel.models.rule.RuleLocationUpdateInput

/**
 * Controller for managing rules.
 *
 * @property ruleService the rule service
 */
@RestController
@RequestMapping("api/rule")
class RuleController(
    private val ruleService: RuleService,
    private val ruleErrorHandler: RuleErrorHandler,
) {
    /**
     * Creates a new location rule.
     *
     * @param authUser the authenticated user
     * @param rule the rule input
     * @return the response entity with the created rule
     */
    @PostMapping("/location")
    fun createRuleLocation(
        authUser: AuthenticatedUser,
        @RequestBody rule: RuleLocationInput,
    ): ResponseEntity<*> {
        val result: Either<RuleError, RuleLocation> =
            ruleService.createLocationRule(
                userId = authUser.user.id,
                startTime = rule.startTime,
                endTime = rule.endTime,
                locationId = rule.locationId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure ->
                ruleErrorHandler.toResponse(result.value, rule.locationId.toString())
        }
    }

    /**
     * Creates a new event rule.
     *
     * @param authUser the authenticated user
     * @param rule the rule input
     * @return the response entity with the created rule
     */
    @PostMapping("/event")
    fun createRuleEvent(
        authUser: AuthenticatedUser,
        @RequestBody rule: RuleEventInput,
    ): ResponseEntity<*> {
        val result: Either<RuleError, RuleEvent> =
            ruleService.createRuleEvent(
                userId = authUser.user.id,
                eventId = rule.eventId,
                startTime = rule.startTime,
                endTime = rule.endTime,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure ->
                ruleErrorHandler.toResponse(
                    ruleError = result.value,
                    input = rule.eventId.toString(),
                )
        }
    }

    /**
     * Updates the time of an event rule
     *
     * @param authUser the authenticated user
     * @param rule the rule input
     * @return the response entity with the updated rule
     */
    @PutMapping("/event/{id}/update/time")
    fun updateRuleEvent(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
        @RequestBody rule: RuleEventUpdateInput,
    ): ResponseEntity<*> {
        val result: Either<RuleError, RuleEvent> =
            ruleService.updateEventRule(
                userId = authUser.user.id,
                ruleId = id,
                startTime = rule.startTime,
                endTime = rule.endTime,
            )
        return when (result) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    RuleEventOutput(
                        id = result.value.id,
                        startTime = result.value.startTime,
                        endTime = result.value.endTime,
                        event = result.value.event,
                    ),
                )
            is Failure ->
                ruleErrorHandler.toResponse(
                    ruleError = result.value,
                    input = id.toString(),
                )
        }
    }

    /**
     * Updates the location rule time.
     *
     * @param authUser the authenticated user
     * @param id the rule ID
     * @param rule the rule input
     * @return the response entity with the updated rule
     */
    @PutMapping("/location/{id}/update")
    fun updateRuleLocation(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
        @RequestBody rule: RuleLocationUpdateInput,
    ): ResponseEntity<*> {
        val result: Either<RuleError, RuleLocation> =
            ruleService.updateLocationRule(
                userId = authUser.user.id,
                ruleId = id,
                startTime = rule.startTime,
                endTime = rule.endTime,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                ruleErrorHandler.toResponse(
                    ruleError = result.value,
                    input = id.toString(),
                )
        }
    }

    /**
     * Gets a location rule by ID.
     *
     * @param authUser the authenticated user
     * @param id the rule ID
     * @return the response entity with the rule
     */
    @GetMapping("/location/{id}")
    fun getRuleLocation(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result: Either<RuleError, RuleLocation> =
            ruleService.getLocationRuleById(
                userId = authUser.user.id,
                ruleId = id,
            )
        return when (result) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    RuleLocationOutput(
                        id = result.value.id,
                        startTime = result.value.startTime,
                        endTime = result.value.endTime,
                        location = result.value.location,
                    ),
                )
            is Failure ->
                ruleErrorHandler.toResponse(
                    ruleError = result.value,
                    input = id.toString(),
                )
        }
    }

    /**
     * Gets an event rule by ID.
     *
     * @param authUser the authenticated user
     * @param id the rule ID
     * @return the response entity with the rule
     */
    @GetMapping("/event/{id}")
    fun getRuleEvent(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result: Either<RuleError, RuleEvent> =
            ruleService.getEventRuleById(
                userId = authUser.user.id,
                ruleId = id,
            )
        return when (result) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    RuleEventOutput(
                        id = result.value.id,
                        startTime = result.value.startTime,
                        endTime = result.value.endTime,
                        event = result.value.event,
                    ),
                )
            is Failure ->
                ruleErrorHandler.toResponse(
                    ruleError = result.value,
                    input = id.toString(),
                )
        }
    }

    /**
     * Gets all rules from the authenticated user.
     *
     * @param authUser the authenticated user
     * @return the response entity with the list of rules
     */
    @GetMapping("/all")
    fun getAllRulesFromUser(authUser: AuthenticatedUser): ResponseEntity<*> {
        val result = ruleService.getRulesByUser(authUser.user.id)
        return when (result) {
            is Success -> {
                val ruleLocationOutput =
                    result.value.filterIsInstance<RuleLocation>().map {
                        RuleLocationOutput(it.id, it.startTime, it.endTime, it.location)
                    }
                val ruleEventOutput =
                    result.value.filterIsInstance<RuleEvent>().map {
                        RuleEventOutput(it.id, it.startTime, it.endTime, it.event)
                    }
                ResponseEntity.ok(
                    RuleListOutput(
                        eventRulesN = ruleEventOutput.size,
                        eventRules = ruleEventOutput,
                        locationRulesN = ruleLocationOutput.size,
                        locationRules = ruleLocationOutput,
                    ),
                )
            }

            is Failure -> {
                ruleErrorHandler.toResponse(
                    ruleError = result.value,
                )
            }
        }
    }

    /**
     * Deletes a location rule by ID.
     *
     * @param authUser the authenticated user
     * @param id the rule ID
     * @return the response entity with an empty body
     */
    @DeleteMapping("/event/{id}")
    fun deleteRuleEvent(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result: Either<RuleError, Unit> =
            ruleService.deleteRuleEvent(
                userId = authUser.user.id,
                ruleId = id,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure ->
                ruleErrorHandler.toResponse(result.value, id.toString())
        }
    }

    /**
     * Deletes a location rule by ID.
     *
     * @param authUser the authenticated user
     * @param id the rule ID
     * @return the response entity with an empty body
     */
    @DeleteMapping("/location/{id}")
    fun deleteRuleLocation(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result: Either<RuleError, Unit> =
            ruleService.deleteLocationRule(
                userId = authUser.user.id,
                ruleId = id,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure ->
                ruleErrorHandler.toResponse(result.value, id.toString())
        }
    }
}
