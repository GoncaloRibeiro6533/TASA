package pt.isel

import jakarta.inject.Named
import pt.isel.transaction.TransactionManager
import java.time.LocalDateTime

/**
 * Represents the possible errors that can occur while applying or validating rules.
 * This is a sealed class, with specific error types defined as data objects inherited from it.
 */
sealed class RuleError {
    data object NegativeIdentifier : RuleError()

    data object UserNotFound : RuleError()

    data object RuleAlreadyExistsForGivenTime : RuleError()

    data object RuleNotFound : RuleError()

    data object TitleCannotBeBlank : RuleError()

    data object StartTimeMustBeBeforeEndTime : RuleError()

    data object EndTimeMustBeBeforeEndTime : RuleError()

    data object InvalidCoordinate : RuleError()

    data object InvalidLatitude : RuleError()

    data object InvalidLongitude : RuleError()

    data object InvalidRadius : RuleError()

    data object NotAllowed : RuleError()

    data object EventNotFound : RuleError()

    data object LocationNotFound : RuleError()
}

/**
 * Service responsible for managing and manipulating rules.
 * Rules can be associated with events or locations
 * and are specific to a user.
 * This class uses a transactional context to ensure consistent state changes.
 *
 * @constructor
 * @property trxManager Manages transactional contexts for database operations.
 */
@Named
class RuleService(
    private val trxManager: TransactionManager,
) {
    /**
     * Creates a new event rule for a specific user and associates it with the specified event
     * and time range.
     *
     * @param userId the ID of the user for whom the rule is being created
     * @param eventId the ID of the event associated with the rule
     * @param calendarId the ID of the calendar associated with the event
     * @param title the title of the event
     * @param startTime the start time of the event
     * @param endTime the end time of the event
     * @return an [Either] instance containing either the created [Rule] wrapped in [Either.Right]
     *         or an instance of [RuleError] wrapped in [Either.Left] if validation fails
     */
    fun createRuleEvent(
        userId: Int,
        eventId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Either<RuleError, RuleEvent> =
        trxManager.run {
            if (userId < 0 || eventId < 0) {
                return@run failure(RuleError.NegativeIdentifier)
            }
            if (startTime > endTime || startTime == endTime) {
                return@run failure(RuleError.StartTimeMustBeBeforeEndTime)
            }
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            if (ruleRepo.findByUserId(user).filterIsInstance<RuleEvent>().any {
                    checkCollisionTime(
                        it.startTime,
                        it.endTime,
                        startTime,
                        endTime,
                    )
                }
            ) {
                return@run failure(RuleError.RuleAlreadyExistsForGivenTime)
            }
            val event =
                eventRepo.findById(eventId)
                    ?: return@run failure(RuleError.EventNotFound)
            if (!eventRepo.findByUserId(user).contains(event)) {
                return@run failure(RuleError.NotAllowed)
            }
            val rule =
                ruleRepo.createEventRule(
                    event,
                    user,
                    startTime,
                    endTime,
                )
            success(rule)
        }

    /**
     * Creates a new location-based rule.
     *
     * @param userId the ID of the user for whom the rule is being created
     * @param title the title of the location rule
     * @param name the name of the location
     * @return an [Either] instance containing either the created [RuleLocation] wrapped in [Either.Right]
     *         or an instance of [RuleError] wrapped in [Either.Left] if validation fails
     */
    fun createLocationRule(
        userId: Int,
        locationId: Int,
    ): Either<RuleError, RuleLocation> =
        trxManager.run {
            if (userId < 0) return@run failure(RuleError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            if (ruleRepo.findByUserId(user).filterIsInstance<RuleLocation>().any {
                    it.location.id == locationId
                }
            ) {
                return@run failure(RuleError.RuleAlreadyExistsForGivenTime)
            }
            val location =
                locationRepo.findById(locationId)
                    ?: return@run failure(RuleError.LocationNotFound)
            if (!locationRepo.findByUserId(user).contains(location)) return@run failure(RuleError.NotAllowed)
            val rule =
                ruleRepo.createLocationRule(
                    location,
                    user,
                )
            success(rule)
        }

    /**
     * Retrieves a specific event rule by its ID.
     *
     * @param userId the ID of the user who owns the rule
     * @param ruleId the ID of the rule to be retrieved
     * @return an [Either] instance containing the [RuleEvent] wrapped in [Either.Right]
     */
    fun getEventRuleById(
        userId: Int,
        ruleId: Int,
    ): Either<RuleError, RuleEvent> =
        trxManager.run {
            if (userId < 0 || ruleId < 0) return@run failure(RuleError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            val rule = ruleRepo.findRuleEventById(ruleId) ?: return@run failure(RuleError.RuleNotFound)
            if (rule.creator != user) return@run failure(RuleError.NotAllowed)
            return@run success(rule)
        }

    /**
     * Retrieves a specific location rule by its ID.
     *
     * @param userId the ID of the user who owns the rule
     * @param ruleId the ID of the rule to be retrieved
     * @return an [Either] instance containing the [RuleLocation] wrapped in [Either.Right]
     */
    fun getLocationRuleById(
        userId: Int,
        ruleId: Int,
    ): Either<RuleError, RuleLocation> =
        trxManager.run {
            if (userId < 0 || ruleId < 0) return@run failure(RuleError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            val rule = ruleRepo.findRuleLocationById(ruleId) ?: return@run failure(RuleError.RuleNotFound)
            if (rule.creator != user) return@run failure(RuleError.NotAllowed)
            return@run success(rule)
        }

    /**
     * Retrieves the list of rules associated with a specific user.
     *
     * @param userId the ID of the user for whom the rules are being retrieved.
     *               If the ID is negative, a [RuleError.NegativeIdentifier] is returned.
     * @return an [Either] instance containing a list of [Rule] objects wrapped in [Either.Right]
     *         if successful, or a [RuleError] wrapped in [Either.Left] in case of failure.
     */
    fun getRulesByUser(userId: Int): Either<RuleError, List<Rule>> =
        trxManager.run {
            if (userId < 0) return@run failure(RuleError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            val rules = ruleRepo.findByUserId(user)
            return@run success(rules)
        }

    /**
     * Updates the specified event rule with new start and end times.
     *
     * @param userId the ID of the user who owns the rule
     * @param ruleId the ID of the rule to be updated
     * @param startTime the new start time for the rule
     * @param endTime the new end time for the rule
     * @return an [Either] instance containing either the updated [RuleEvent] wrapped in [Either.Right]
     *         or an instance of [RuleError] wrapped in [Either.Left] if any validation fails
     */
    fun updateEventRule(
        userId: Int,
        ruleId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Either<RuleError, RuleEvent> =
        trxManager.run {
            if (startTime >= endTime) return@run failure(RuleError.StartTimeMustBeBeforeEndTime)
            if (userId < 0 || ruleId < 0) return@run failure(RuleError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            val rule = ruleRepo.findRuleEventById(ruleId) ?: return@run failure(RuleError.RuleNotFound)
            val userRules = ruleRepo.findByUserId(user).filterIsInstance<RuleEvent>()
            if (userRules.any {
                    it.id != ruleId &&
                        checkCollisionTime(
                            it.startTime,
                            it.endTime,
                            startTime,
                            endTime,
                        )
                }
            ) {
                return@run failure(RuleError.RuleAlreadyExistsForGivenTime)
            }
            if (rule.creator != user) return@run failure(RuleError.NotAllowed)
            val updatedRule = ruleRepo.updateRuleEvent(rule, startTime, endTime)
            return@run success(updatedRule)
        }

    /**
     * Deletes an existing rule associated with a specific user.
     *
     * @param userId the ID of the user who owns the rule
     * @param ruleId the ID of the rule to be deleted
     * @return an [Either] instance containing [Unit] wrapped in [Either.Right] if the rule was successfully deleted,
     *         or an instance of [RuleError] wrapped in [Either.Left] if the operation failed
     */
    fun deleteRuleEvent(
        userId: Int,
        ruleId: Int,
    ): Either<RuleError, Unit> =
        trxManager.run {
            if (userId < 0 || ruleId < 0) return@run failure(RuleError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            val rule =
                ruleRepo.findRuleEventById(ruleId)
                    ?: return@run failure(RuleError.RuleNotFound)
            if (rule.creator != user) return@run failure(RuleError.NotAllowed)
            ruleRepo.deleteRuleEvent(rule)
            return@run success(Unit)
        }

    /**
     * Deletes an existing location rule associated with a specific user.
     *
     * @param userId the ID of the user who owns the rule
     * @param ruleId the ID of the rule to be deleted
     * @return an [Either] instance containing [Unit] wrapped in [Either.Right] if the rule was successfully deleted,
     *        or an instance of [RuleError] wrapped in [Either.Left] if the operation failed
     */
    fun deleteLocationRule(
        userId: Int,
        ruleId: Int,
    ): Either<RuleError, Unit> =
        trxManager.run {
            if (userId < 0 || ruleId < 0) return@run failure(RuleError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(RuleError.UserNotFound)
            val rule = ruleRepo.findRuleLocationById(ruleId) ?: return@run failure(RuleError.RuleNotFound)
            if (rule.creator != user) {
                return@run failure(RuleError.NotAllowed)
            }
            ruleRepo.deleteLocationEvent(rule)
            return@run success(Unit)
        }

    fun checkCollisionTime(
        startTimeX: LocalDateTime,
        endTimeX: LocalDateTime,
        startTimeY: LocalDateTime,
        endTimeY: LocalDateTime,
    ): Boolean {
        return startTimeX <= endTimeY && endTimeX >= startTimeY
    }
}
