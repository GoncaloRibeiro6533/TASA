package pt.isel.rule

import pt.isel.Event
import pt.isel.Location
import pt.isel.Rule
import pt.isel.RuleEvent
import pt.isel.RuleLocation
import pt.isel.User
import java.time.LocalDateTime

/**
 * Interface that defines the operations that can be done on the Rule repository.
 */
interface RuleRepository {
    fun createEventRule(
        event: Event,
        user: User,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): RuleEvent

    fun createLocationRule(
        location: Location,
        user: User,
    ): RuleLocation

    fun findAll(): List<Rule>

    fun findRuleEventById(id: Int): RuleEvent?

    fun findRuleLocationById(id: Int): RuleLocation?

    fun findByUserId(user: User): List<Rule>

    fun updateRuleEvent(
        rule: RuleEvent,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): RuleEvent

    fun deleteRuleEvent(rule: RuleEvent): Boolean

    fun deleteLocationEvent(rule: RuleLocation): Boolean

    fun clear()
}
