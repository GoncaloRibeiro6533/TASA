package exclusion

import pt.isel.AppExclusion
import pt.isel.ContactExclusion
import pt.isel.Exclusion
import pt.isel.User

/**
 * Interface that defines the operations that can be done on the Exclusion repository.
 */
interface ExclusionRepository {
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

    fun findContactExclusionsByUserId(user: User): List<ContactExclusion>

    fun findAllExclusionsByUserId(user: User): List<Exclusion>

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
}
