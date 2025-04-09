package pt.isel.transaction

import pt.isel.event.EventRepository
import pt.isel.exclusion.ExclusionRepository
import pt.isel.location.LocationRepository
import pt.isel.rule.RuleRepository
import pt.isel.session.SessionRepository
import pt.isel.user.UserRepository

class TransactionInMem(
    override val userRepo: UserRepository,
    override val sessionRepo: SessionRepository,
    override val ruleRepo: RuleRepository,
    override val exclusionRepo: ExclusionRepository,
    override val eventRepo: EventRepository,
    override val locationRepo: LocationRepository,
) : Transaction {
    override fun rollback() {
        throw UnsupportedOperationException()
    }
}
