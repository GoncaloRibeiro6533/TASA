package pt.isel

import org.jdbi.v3.core.Handle
import pt.isel.event.EventRepository
import pt.isel.location.LocationRepository
import pt.isel.rule.RuleRepository
import pt.isel.session.SessionRepository
import pt.isel.transaction.Transaction
import pt.isel.user.UserRepository

class TransactionJdbi(
    private val handle: Handle,
) : Transaction {
    override val userRepo: UserRepository = JdbiUserRepository(handle)
    override val sessionRepo: SessionRepository = JdbiSessionRepository(handle)
    override val ruleRepo: RuleRepository = JdbiRuleRepository(handle)
    override val eventRepo: EventRepository = JdbiEventRepository(handle)
    override val locationRepo: LocationRepository = JdbiLocationRepository(handle)

    override fun rollback() {
        handle.rollback()
    }
}
