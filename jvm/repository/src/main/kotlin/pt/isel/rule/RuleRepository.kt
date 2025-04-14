package pt.isel.rule

import kotlinx.datetime.Instant
import pt.isel.Event
import pt.isel.Location
import pt.isel.Rule
import pt.isel.RuleEvent
import pt.isel.RuleLocation
import pt.isel.User

/**
 * Interface that defines the operations that can be done on the Rule repository.
 */
interface RuleRepository {
    fun createEventRule(
        event: Event,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): RuleEvent

    fun createLocationRule(
        location: Location,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): RuleLocation

    fun findAll(): List<Rule>

    fun findRuleEventById(id: Int): RuleEvent?

    fun findRuleLocationById(id: Int): RuleLocation?

    fun findByUserId(user: User): List<Rule>

    fun updateRuleEvent(
        rule: RuleEvent,
        startTime: Instant,
        endTime: Instant,
    ): RuleEvent

    fun updateRuleLocation(
        rule: RuleLocation,
        startTime: Instant,
        endTime: Instant,
    ): RuleLocation

    fun deleteRuleEvent(rule: RuleEvent): Boolean

    fun deleteLocationEvent(rule: RuleLocation): Boolean

    fun clear()
}
