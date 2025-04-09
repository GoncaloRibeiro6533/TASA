package pt.isel.transaction

import pt.isel.event.EventRepository
import pt.isel.exclusion.ExclusionRepository
import pt.isel.location.LocationRepository
import pt.isel.rule.RuleRepository
import pt.isel.session.SessionRepository
import pt.isel.user.UserRepository

interface Transaction {
    val userRepo: UserRepository
    val sessionRepo: SessionRepository
    val ruleRepo: RuleRepository
    val exclusionRepo: ExclusionRepository
    val eventRepo: EventRepository
    val locationRepo: LocationRepository

    fun rollback()
}
