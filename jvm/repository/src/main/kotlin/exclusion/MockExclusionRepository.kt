package exclusion

import pt.isel.AppExclusion
import pt.isel.ContactExclusion
import pt.isel.Exclusion
import pt.isel.User

class MockExclusionRepository : ExclusionRepository {
    private var appExclusionId = 0
    private var contactExclusionId = 0
    private val appExclusions = mutableMapOf<Int, MutableList<AppExclusion>>()
    private val contactExclusions = mutableMapOf<Int, MutableList<ContactExclusion>>()

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

    override fun findContactExclusionsByUserId(user: User): List<ContactExclusion> {
        return contactExclusions[user.id]?.toList() ?: emptyList()
    }

    override fun findAllExclusionsByUserId(user: User): List<Exclusion> {
        val appExclusionsList = appExclusions[user.id]?.toList() ?: emptyList()
        val contactExclusionsList = contactExclusions[user.id]?.toList() ?: emptyList()
        return appExclusionsList + contactExclusionsList
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
}
