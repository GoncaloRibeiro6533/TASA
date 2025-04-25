package pt.isel

import jakarta.inject.Named
import pt.isel.transaction.TransactionManager

sealed class ExclusionError {
    data object AppNameTooLong : ExclusionError()

    data object AppNameBlank : ExclusionError()

    data object UserNotFound : ExclusionError()

    data object ExclusionAlreadyExists : ExclusionError()

    data object ExclusionNotFound : ExclusionError()

    data object BlankContactName : ExclusionError()

    data object BlankPhoneNumber : ExclusionError()

    data object NegativeIdentifier : ExclusionError()

    data object ContactNameTooLong : ExclusionError()

    data object PhoneNumberTooLong : ExclusionError()

    data object NotAllowed : ExclusionError()

    data object RuleNotFound : ExclusionError()
}

/**
 * Service that manages exclusions for users. This includes operations
 * for managing contact and app exclusions, such as creation, retrieval,
 * deletion, and updating of exclusions.
 *
 * @constructor Initializes the `ExclusionService` with the provided `TransactionManager`.
 *
 * @property trxManager The transaction manager used to execute operations within a transactional context.
 */
@Named
class ExclusionService(
    private val trxManager: TransactionManager,
) {
    /**
     * Creates a contact exclusion for a specific user.
     *
     * @param userId The unique identifier of the user for whom the contact exclusion is being created.
     * @param contactName The name of the contact to exclude.
     * @param contactPhone The phone number of the contact to exclude.
     * @return Either an ExclusionError indicating the failure reason,
     * or a ContactExclusion object representing the successfully created exclusion.
     */
    fun createContactExclusion(
        userId: Int,
        contactName: String,
        contactPhone: String,
    ): Either<ExclusionError, ContactExclusion> =
        trxManager.run {
            if (userId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            if (contactName.isBlank()) return@run failure(ExclusionError.BlankContactName)
            if (contactPhone.isBlank()) return@run failure(ExclusionError.BlankPhoneNumber)
            if (contactName.length > ContactExclusion.MAX_NAME_LENGTH) {
                return@run failure(ExclusionError.ContactNameTooLong)
            }
            if (contactPhone.length > ContactExclusion.MAX_PHONE_NUMBER_LENGTH) {
                return@run failure(ExclusionError.PhoneNumberTooLong)
            }
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(ExclusionError.UserNotFound)
            if (exclusionRepo.findContactExclusionsByUserId(user)
                    .any { it.name == contactName && it.phoneNumber == contactPhone }
            ) {
                return@run failure(ExclusionError.ExclusionAlreadyExists)
            }
            val exclusion =
                exclusionRepo.createContactExclusion(
                    contactName = contactName,
                    phoneNumber = contactPhone,
                    user = user,
                )
            success(exclusion)
        }

    /**
     * Creates an app exclusion for a specific user.
     *
     * @param userId The unique identifier of the user for whom the app exclusion is being created.
     * @param appName The name of the app to exclude.
     * @return Either an [ExclusionError] indicating the failure reason, or an [AppExclusion]
     * object representing the successfully created exclusion.
     */
    fun createAppExclusion(
        userId: Int,
        appName: String,
    ): Either<ExclusionError, AppExclusion> =
        trxManager.run {
            if (userId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            if (appName.isBlank()) return@run failure(ExclusionError.AppNameBlank)
            if (appName.length > AppExclusion.MAX_NAME_LENGTH) {
                return@run failure(ExclusionError.AppNameTooLong)
            }
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(ExclusionError.UserNotFound)
            if (exclusionRepo.findAppExclusionsByUserId(user)
                    .any { it.name == appName }
            ) {
                return@run failure(ExclusionError.ExclusionAlreadyExists)
            }
            val exclusion =
                exclusionRepo.createAppExclusion(
                    appName = appName,
                    user = user,
                )
            success(exclusion)
        }

    /**
     * Retrieves a list of exclusions associated with a specified user.
     *
     * @param userId The unique identifier of the user whose exclusions are being retrieved.
     *               Must be a non-negative integer.
     * @return Either an [ExclusionError] if the operation fails, or a list of [Exclusion]
     *         representing the user's exclusions if the operation succeeds.
     */
    fun getUserExclusions(userId: Int): Either<ExclusionError, List<Exclusion>> =
        trxManager.run {
            if (userId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(ExclusionError.UserNotFound)
            val exclusions = exclusionRepo.findAllExclusionsByUserId(user)
            success(exclusions)
        }

    fun getExclusionsOfRuleEvent(
        userId: User,
        ruleId: Int,
    ): Either<ExclusionError, List<Exclusion>> =
        trxManager.run {
            if (userId.id < 0) return@run failure(ExclusionError.NegativeIdentifier)
            if (ruleId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val rule =
                ruleRepo.findRuleEventById(ruleId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val user =
                userRepo.findById(userId.id)
                    ?: return@run failure(ExclusionError.UserNotFound)
            if (user.id != rule.creator.id) return@run failure(ExclusionError.NotAllowed)
            val exclusions = exclusionRepo.findExclusionsByRuleId(rule)
            success(exclusions)
        }

    fun getExclusionsOfRuleLocation(
        userId: User,
        ruleId: Int,
    ): Either<ExclusionError, List<Exclusion>> =
        trxManager.run {
            if (userId.id < 0) return@run failure(ExclusionError.NegativeIdentifier)
            if (ruleId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val rule =
                ruleRepo.findRuleLocationById(ruleId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val user =
                userRepo.findById(userId.id)
                    ?: return@run failure(ExclusionError.UserNotFound)
            if (user.id != rule.creator.id) return@run failure(ExclusionError.NotAllowed)
            val exclusions = exclusionRepo.findExclusionsByRuleId(rule)
            success(exclusions)
        }

    fun addAppExclusionToRuleEvent(
        userId: User,
        ruleId: Int,
        exclusionId: Int,
    ): Either<ExclusionError, Boolean> =
        trxManager.run {
            if (userId.id < 0 || ruleId < 0 || exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val rule =
                ruleRepo.findRuleEventById(ruleId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val user = userRepo.findById(userId.id) ?: return@run failure(ExclusionError.UserNotFound)
            if (rule.creator.id != user.id) return@run failure(ExclusionError.NotAllowed)
            val exclusion =
                exclusionRepo.findByIdAppExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            if (exclusion !in exclusionRepo.findExclusionsByRuleId(rule)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            val result = exclusionRepo.addAppExclusionToRuleEvent(rule, exclusion)
            success(result)
        }

    fun addAppExclusionToRuleLocation(
        userId: User,
        ruleId: Int,
        exclusionId: Int,
    ): Either<ExclusionError, Boolean> =
        trxManager.run {
            if (userId.id < 0 || ruleId < 0 || exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val rule =
                ruleRepo.findRuleLocationById(ruleId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val user = userRepo.findById(userId.id) ?: return@run failure(ExclusionError.UserNotFound)
            if (rule.creator.id != user.id) return@run failure(ExclusionError.NotAllowed)
            val exclusion: AppExclusion =
                exclusionRepo.findByIdAppExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            if (exclusion !in exclusionRepo.findExclusionsByRuleId(rule)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            val result = exclusionRepo.addAppExclusionToRuleLocation(rule, exclusion)
            success(result)
        }

    fun addContactExclusionToRuleEvent(
        userId: User,
        ruleId: Int,
        exclusionId: Int,
    ): Either<ExclusionError, Boolean> =
        trxManager.run {
            if (userId.id < 0 || ruleId < 0 || exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val rule =
                ruleRepo.findRuleEventById(ruleId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val user = userRepo.findById(userId.id) ?: return@run failure(ExclusionError.UserNotFound)
            if (rule.creator.id != user.id) return@run failure(ExclusionError.NotAllowed)
            val exclusion =
                exclusionRepo.findByIdContactExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            if (exclusion !in exclusionRepo.findExclusionsByRuleId(rule)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            val result = exclusionRepo.addContactExclusionToRuleEvent(rule, exclusion)
            success(result)
        }

    fun addContactExclusionToRuleLocation(
        userId: User,
        ruleId: Int,
        exclusionId: Int,
    ): Either<ExclusionError, Boolean> =
        trxManager.run {
            if (userId.id < 0 || ruleId < 0 || exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val rule =
                ruleRepo.findRuleLocationById(ruleId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val user = userRepo.findById(userId.id) ?: return@run failure(ExclusionError.UserNotFound)
            if (rule.creator.id != user.id) return@run failure(ExclusionError.NotAllowed)
            val exclusion =
                exclusionRepo.findByIdContactExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            if (exclusion !in exclusionRepo.findExclusionsByRuleId(rule)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            val result = exclusionRepo.addContactExclusionToRuleLocation(rule, exclusion)
            success(result)
        }

    /**
     * Deletes an app exclusion for a specified user.
     *
     * @param userId The unique identifier of the user whose app exclusion is to be deleted.
     *               Must be a non-negative integer.
     * @param exclusionId The unique identifier of the app exclusion to be deleted.
     *                    Must be a non-negative integer.
     * @return Either an [ExclusionError] indicating the failure reason or a Boolean indicating
     *         whether the app exclusion was successfully deleted.
     */
    fun deleteAppExclusion(
        userId: Int,
        exclusionId: Int,
    ): Either<ExclusionError, Boolean> =
        trxManager.run {
            if (userId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            if (exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(ExclusionError.UserNotFound)
            val exclusion =
                exclusionRepo.findAppExclusionsByUserId(user).find { it.id == exclusionId }
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val deleted = exclusionRepo.deleteAppExclusion(exclusion)
            success(deleted)
        }

    /**
     * Deletes a specific contact exclusion for a user.
     *
     * @param userId The unique identifier of the user whose contact exclusion is to be deleted.
     *               Must be a non-negative integer.
     * @param exclusionId The unique identifier of the contact exclusion to be deleted.
     *                    Must be a non-negative integer.
     * @return Either an [ExclusionError] indicating the failure reason, or a [Boolean] indicating
     *         whether the contact exclusion was successfully deleted.
     */
    fun deleteContactExclusion(
        userId: Int,
        exclusionId: Int,
    ): Either<ExclusionError, Boolean> =
        trxManager.run {
            if (userId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ExclusionError.UserNotFound)
            if (exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val exclusion =
                exclusionRepo.findContactExclusionsByUserId(user).find { it.id == exclusionId }
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val result = exclusionRepo.deleteContactExclusion(exclusion)
            success(result)
        }

    /**
     * Updates an existing contact exclusion for a specific user.
     *
     * @param userId The unique identifier of the user whose contact exclusion is to be updated.
     * Must be a non-negative integer.
     * @param exclusionId The unique identifier of the contact exclusion to update. Must be a non-negative integer.
     * @param contactName The updated name of the excluded contact.
     * Cannot be blank and must not exceed the maximum allowed length.
     * @param phoneNumber The updated phone number of the excluded contact.
     * Cannot be blank and must not exceed the maximum allowed length.
     * @return Either an [ExclusionError] indicating the failure reason,
     * or a [ContactExclusion] object representing the successfully updated exclusion.
     */
    fun updateContactExclusion(
        userId: Int,
        exclusionId: Int,
        contactName: String?,
        phoneNumber: String?,
    ): Either<ExclusionError, ContactExclusion> =
        trxManager.run {
            if (userId < 0 || exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            if (contactName != null) {
                if (contactName.isBlank()) return@run failure(ExclusionError.BlankContactName)
                if (contactName.length > ContactExclusion.MAX_NAME_LENGTH) {
                    return@run failure(ExclusionError.ContactNameTooLong)
                }
            }
            if (phoneNumber != null) {
                if (phoneNumber.isBlank()) return@run failure(ExclusionError.BlankPhoneNumber)
                if (phoneNumber.length > ContactExclusion.MAX_PHONE_NUMBER_LENGTH) {
                    return@run failure(ExclusionError.PhoneNumberTooLong)
                }
            }
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(ExclusionError.UserNotFound)
            val exclusion =
                exclusionRepo.findContactExclusionsByUserId(user).find { it.id == exclusionId }
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val result =
                exclusionRepo.updateContactExclusion(
                    contactExclusion = exclusion,
                    contactName = contactName ?: exclusion.name,
                    phoneNumber = phoneNumber ?: exclusion.phoneNumber,
                )
            success(result)
        }

    fun addExclusionContactToRuleEvent(
        userId: Int,
        exclusionId: Int,
        ruleId: Int,
    ): Either<ExclusionError, Unit> =
        trxManager.run {
            if (userId < 0 || exclusionId < 0 || ruleId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ExclusionError.UserNotFound)
            val exclusion =
                exclusionRepo.findByIdContactExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val rule = ruleRepo.findRuleEventById(ruleId) ?: return@run failure(ExclusionError.RuleNotFound)
            if (user.id != rule.creator.id) return@run failure(ExclusionError.NotAllowed)
            if (exclusion !in exclusionRepo.findContactExclusionsByUserId(user)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            exclusionRepo.addContactExclusionToRuleEvent(rule, exclusion)
            success(Unit)
        }

    fun addExclusionContactToRuleLocation(
        userId: Int,
        exclusionId: Int,
        ruleId: Int,
    ): Either<ExclusionError, Unit> =
        trxManager.run {
            if (userId < 0 || exclusionId < 0 || ruleId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ExclusionError.UserNotFound)
            val exclusion =
                exclusionRepo.findByIdContactExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val rule = ruleRepo.findRuleLocationById(ruleId) ?: return@run failure(ExclusionError.RuleNotFound)
            if (user.id != rule.creator.id) return@run failure(ExclusionError.NotAllowed)
            if (exclusion !in exclusionRepo.findContactExclusionsByUserId(user)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            exclusionRepo.addContactExclusionToRuleLocation(rule, exclusion)
            success(Unit)
        }

    fun addExclusionAppToRuleEvent(
        userId: Int,
        exclusionId: Int,
        ruleId: Int,
    ): Either<ExclusionError, Unit> =
        trxManager.run {
            if (userId < 0 || exclusionId < 0 || ruleId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ExclusionError.UserNotFound)
            val exclusion =
                exclusionRepo.findByIdAppExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val rule = ruleRepo.findRuleEventById(ruleId) ?: return@run failure(ExclusionError.RuleNotFound)
            if (user.id != rule.creator.id) return@run failure(ExclusionError.NotAllowed)
            if (exclusion !in exclusionRepo.findAppExclusionsByUserId(user)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            exclusionRepo.addAppExclusionToRuleEvent(rule, exclusion)
            success(Unit)
        }

    fun addExclusionAppToRuleLocation(
        userId: Int,
        exclusionId: Int,
        ruleId: Int,
    ): Either<ExclusionError, Unit> =
        trxManager.run {
            if (userId < 0 || exclusionId < 0 || ruleId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(ExclusionError.UserNotFound)
            val exclusion =
                exclusionRepo.findByIdAppExclusions(exclusionId)
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val rule = ruleRepo.findRuleLocationById(ruleId) ?: return@run failure(ExclusionError.RuleNotFound)
            if (user.id != rule.creator.id) return@run failure(ExclusionError.NotAllowed)
            if (exclusion !in exclusionRepo.findAppExclusionsByUserId(user)) {
                return@run failure(ExclusionError.NotAllowed)
            }
            exclusionRepo.addAppExclusionToRuleLocation(rule, exclusion)
            success(Unit)
        }

    /**
     * Updates an existing app exclusion for a specified user.
     *
     * @param userId The unique identifier of the user whose app exclusion is being updated.
     *               Must be a non-negative integer.
     * @param exclusionId The unique identifier of the app exclusion to update.
     *                    Must be a non-negative integer.
     * @param appName The updated name of the excluded app.
     *                Cannot be blank and must not exceed the maximum allowed length.
     * @return Either an [ExclusionError] indicating the failure reason, or an [AppExclusion]
     *         object representing the successfully updated exclusion.
     */
    fun updateAppExclusion(
        userId: Int,
        exclusionId: Int,
        appName: String,
    ): Either<ExclusionError, AppExclusion> =
        trxManager.run {
            if (userId < 0 || exclusionId < 0) return@run failure(ExclusionError.NegativeIdentifier)
            if (appName.isBlank()) return@run failure(ExclusionError.AppNameBlank)
            if (appName.length > AppExclusion.MAX_NAME_LENGTH) {
                return@run failure(ExclusionError.AppNameTooLong)
            }
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(ExclusionError.UserNotFound)
            val exclusion =
                exclusionRepo.findAppExclusionsByUserId(user).find { it.id == exclusionId }
                    ?: return@run failure(ExclusionError.ExclusionNotFound)
            val result =
                exclusionRepo.updateAppExclusion(
                    appExclusion = exclusion,
                    appName = appName,
                )
            success(result)
        }
}
