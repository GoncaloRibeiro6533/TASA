package pt.isel.exclusion

import pt.isel.AppExclusion
import pt.isel.ContactExclusion
import pt.isel.Exclusion
import pt.isel.Rule
import pt.isel.RuleEvent
import pt.isel.RuleLocation
import pt.isel.User
import kotlin.collections.emptyList
import kotlin.collections.get

class MockExclusionRepository : ExclusionRepository {
    private var appExclusionId = 0
    private var contactExclusionId = 0
    private val appExclusions = mutableMapOf<Int, MutableList<AppExclusion>>()
    private val contactExclusions = mutableMapOf<Int, MutableList<ContactExclusion>>()

    // RuleID to List<ExclusionId>
    private val appExclusionsRules = mutableMapOf<Int, MutableList<Int>>()
    private val contactExclusionsRules = mutableMapOf<Int, MutableList<Int>>()

    override fun createAppExclusion(
        appName: String,
        user: User,
    ): AppExclusion {
        val exclusion =
            AppExclusion(
                id = appExclusionId++,
                name = appName,
            )
        appExclusions.computeIfAbsent(user.id) { mutableListOf() }
            .add(exclusion)
        return exclusion
    }

    override fun createContactExclusion(
        contactName: String,
        phoneNumber: String,
        user: User,
    ): ContactExclusion {
        val exclusion =
            ContactExclusion(
                id = contactExclusionId++,
                name = contactName,
                phoneNumber = phoneNumber,
            )
        contactExclusions.computeIfAbsent(user.id) { mutableListOf() }
            .add(exclusion)
        return exclusion
    }

    override fun findAll(): List<Exclusion> {
        return appExclusions.values.flatten() + contactExclusions.values.flatten()
    }

    override fun findAllAppExclusions(): List<AppExclusion> {
        return appExclusions.values.flatten()
    }

    override fun findAllContactExclusions(): List<ContactExclusion> {
        return contactExclusions.values.flatten()
    }

    override fun findByIdAppExclusions(id: Int): AppExclusion? {
        return appExclusions.values.flatten().find { it.id == id }
    }

    override fun findByIdContactExclusions(id: Int): ContactExclusion? {
        return contactExclusions.values.flatten().find { it.id == id }
    }

    override fun findAppExclusionsByUserId(user: User): List<AppExclusion> {
        return appExclusions[user.id]?.toList() ?: emptyList()
    }

    override fun findExclusionsByRuleId(rule: Rule): List<Exclusion> {
        val listAppExclusion = appExclusionsRules[rule.id]?.mapNotNull { findByIdAppExclusions(it) } ?: return emptyList()
        val listContactExclusion = contactExclusionsRules[rule.id]?.mapNotNull { findByIdContactExclusions(it) } ?: return emptyList()
        return listAppExclusion + listContactExclusion
    }

    override fun findContactExclusionsByUserId(user: User): List<ContactExclusion> {
        return contactExclusions[user.id]?.toList() ?: emptyList()
    }

    override fun findAllExclusionsByUserId(user: User): List<Exclusion> {
        val appExclusionsList = appExclusions[user.id]?.toList() ?: emptyList()
        val contactExclusionsList = contactExclusions[user.id]?.toList() ?: emptyList()
        return appExclusionsList + contactExclusionsList
    }

    override fun addAppExclusionToRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        appExclusionsRules.computeIfAbsent(ruleId) { mutableListOf() }
            .add(exclusionId)
        return true
    }

    override fun removeAppExclusionFromRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        val exclusions = appExclusionsRules[ruleId]
        return if (exclusions != null) {
            appExclusionsRules[ruleId]?.remove(exclusionId)
            true
        } else {
            false
        }
    }

    override fun removeContactExclusionFromRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        val exclusions = contactExclusionsRules[ruleId]
        return if (exclusions != null) {
            contactExclusionsRules[ruleId]?.remove(exclusionId)
            true
        } else {
            false
        }
    }

    override fun addContactExclusionToRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        contactExclusionsRules.computeIfAbsent(ruleId) {
            mutableListOf()
        }.add(exclusionId)
        return true
    }

    override fun updateAppExclusion(
        appExclusion: AppExclusion,
        appName: String,
    ): AppExclusion {
        val user = appExclusions.entries.find { it.value.contains(appExclusion) }?.key
        appExclusions[user]?.remove(appExclusion)
        val updatedExclusion = appExclusion.copy(name = appName)
        appExclusions[user]?.add(updatedExclusion)
        return updatedExclusion
    }

    override fun updateContactExclusion(
        contactExclusion: ContactExclusion,
        contactName: String,
        phoneNumber: String,
    ): ContactExclusion {
        val user = contactExclusions.entries.find { it.value.contains(contactExclusion) }?.key
        contactExclusions[user]?.remove(contactExclusion)
        val updatedExclusion = contactExclusion.copy(name = contactName, phoneNumber = phoneNumber)
        contactExclusions[user]?.add(updatedExclusion)
        return updatedExclusion
    }

    override fun deleteAppExclusion(appExclusion: AppExclusion): Boolean {
        val user = appExclusions.entries.find { it.value.contains(appExclusion) }?.key
        return if (user != null) {
            appExclusions[user]?.remove(appExclusion) == true
        } else {
            false
        }
    }

    override fun deleteContactExclusion(contactExclusion: ContactExclusion): Boolean {
        val user = contactExclusions.entries.find { it.value.contains(contactExclusion) }?.key
        return if (user != null) {
            contactExclusions[user]?.remove(contactExclusion) == true
        } else {
            false
        }
    }

    override fun clear() {
        contactExclusions.clear()
        appExclusions.clear()
        appExclusionId = 0
        contactExclusionId = 0
    }

    override fun addAppExclusionToRuleEvent(
        rule: RuleEvent,
        exclusion: AppExclusion,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun addAppExclusionToRuleLocation(
        rule: RuleLocation,
        exclusion: AppExclusion,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun addContactExclusionToRuleEvent(
        rule: RuleEvent,
        exclusion: ContactExclusion,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun addContactExclusionToRuleLocation(
        rule: RuleLocation,
        exclusion: ContactExclusion,
    ): Boolean {
        TODO("Not yet implemented")
    }
}
