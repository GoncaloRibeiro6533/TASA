package pt.isel.transaction

import pt.isel.event.MockEventRepository
import pt.isel.location.MockLocationRepository
import pt.isel.rule.MockRuleRepository
import pt.isel.session.MockSessionRepository
import pt.isel.user.MockUserRepository

class TransactionManagerInMem : TransactionManager {
    private val userRepo = MockUserRepository()
    private val sessionRepo = MockSessionRepository()
    private val ruleRepo = MockRuleRepository()
    private val eventRepo = MockEventRepository()
    private val locationRepo = MockLocationRepository()

    override fun <R> run(block: Transaction.() -> R): R {
        return block(
            TransactionInMem(
                userRepo = userRepo,
                sessionRepo = sessionRepo,
                ruleRepo = ruleRepo,
                eventRepo = eventRepo,
                locationRepo = locationRepo,
            ),
        )
    }
}
