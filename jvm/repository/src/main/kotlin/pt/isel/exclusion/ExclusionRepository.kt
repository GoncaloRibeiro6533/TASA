package pt.isel.exclusion

import pt.isel.AppExclusion
import pt.isel.ContactExclusion
import pt.isel.Exclusion
import pt.isel.Rule
import pt.isel.RuleEvent
import pt.isel.RuleLocation
import pt.isel.User

/**
 * Interface that defines the operations that can be done on the Exclusion repository.
 */
interface ExclusionRepository {
    //    fun createExclusion(
//        exclusion: T,
//        user: User,
//    ): T
//
//    fun findById(id: Int): Exclusion?
//
//    fun findAll(): List<Exclusion>
//
//    fun findByUserId(user: User): List<Exclusion>
//
//    fun findByRuleId(rule: Rule): List<Exclusion>
//
//    fun update
//
//
//
//
    fun createAppExclusion(
        appName: String,
        user: User,
    ): AppExclusion

    fun createContactExclusion(
        contactName: String,
        phoneNumber: String,
        user: User,
    ): ContactExclusion

    fun findAll(): List<Exclusion>

    fun findAllAppExclusions(): List<AppExclusion>

    fun findAllContactExclusions(): List<ContactExclusion>

    fun findByIdAppExclusions(id: Int): AppExclusion?

    fun findByIdContactExclusions(id: Int): ContactExclusion?

    fun findAppExclusionsByUserId(user: User): List<AppExclusion>

    fun findExclusionsByRuleId(rule: Rule): List<Exclusion>

    fun findContactExclusionsByUserId(user: User): List<ContactExclusion>

    fun findAllExclusionsByUserId(user: User): List<Exclusion>

    fun addAppExclusionToRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean

    fun removeAppExclusionFromRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean

    fun removeContactExclusionFromRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean

    fun addContactExclusionToRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean

    fun updateAppExclusion(
        appExclusion: AppExclusion,
        appName: String,
    ): AppExclusion

    fun updateContactExclusion(
        contactExclusion: ContactExclusion,
        contactName: String,
        phoneNumber: String,
    ): ContactExclusion

    fun deleteAppExclusion(appExclusion: AppExclusion): Boolean

    fun deleteContactExclusion(contactExclusion: ContactExclusion): Boolean

    fun clear()

    fun addAppExclusionToRuleEvent(
        rule: RuleEvent,
        exclusion: AppExclusion,
    ): Boolean

    fun addAppExclusionToRuleLocation(
        rule: RuleLocation,
        exclusion: AppExclusion,
    ): Boolean

    fun addContactExclusionToRuleEvent(
        rule: RuleEvent,
        exclusion: ContactExclusion,
    ): Boolean

    fun addContactExclusionToRuleLocation(
        rule: RuleLocation,
        exclusion: ContactExclusion,
    ): Boolean
}
