package rule

import kotlinx.datetime.Instant
import pt.isel.Event
import pt.isel.Rule
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
    ): Rule

    fun createLocationRule(
        locationId: Int,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): Rule

    fun findAll(): List<Rule>

    fun findById(id: Int): Rule?

    fun findByUserId(user: User): List<Rule>

    fun update(
        rule: Rule,
        startTime: Instant,
        endTime: Instant,
    ): Rule

    fun delete(rule: Rule): Boolean

    fun clear()
}
