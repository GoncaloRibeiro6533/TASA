package pt.isel

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pt.isel.transaction.TransactionManager

@Named
class RuleService(
    private val trxManager: TransactionManager,
    private val clock: Clock,
) {
    fun getAllRules(): List<Rule> {
        return trxManager.run {
            ruleRepo.findAll()
        }
    }

    fun createEventRule(
        event: Event,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): Rule {
        return trxManager.run {
            ruleRepo.createEventRule(event, user, startTime, endTime)
        }
    }

    fun createLocationRule(
        locationId: Int,
        user: User,
        startTime: Instant,
        endTime: Instant,
    ): Rule {
        return trxManager.run {
            ruleRepo.createLocationRule(locationId, user, startTime, endTime)
        }
    }
}
