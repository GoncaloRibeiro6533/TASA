package pt.isel

import org.jdbi.v3.core.Handle
import pt.isel.exclusion.ExclusionRepository

class JdbiExclusionRepository(
    private val handle: Handle,
) : ExclusionRepository {
    override fun createAppExclusion(
        appName: String,
        user: User,
    ): AppExclusion {
        val id =
            handle.createUpdate(
                """
                INSERT INTO ps.EXCEPTION_APP (app_name, user_id) 
                VALUES (:appName, :userId)
                """.trimIndent(),
            )
                .bind("appName", appName)
                .bind("userId", user.id)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Int::class.java)
                .one()
        return AppExclusion(
            id = id,
            name = appName,
        )
    }

    override fun createContactExclusion(
        contactName: String,
        phoneNumber: String,
        user: User,
    ): ContactExclusion {
        val id =
            handle.createUpdate(
                """
                INSERT INTO ps.EXCEPTION_CONTACT (name, phone_number, user_id) 
                VALUES (:contactName, :phoneNumber, :userId)
                """.trimIndent(),
            )
                .bind("contactName", contactName)
                .bind("phoneNumber", phoneNumber)
                .bind("userId", user.id)
                .executeAndReturnGeneratedKeys("id")
                .mapTo(Int::class.java)
                .one()
        return ContactExclusion(
            id = id,
            name = contactName,
            phoneNumber = phoneNumber,
        )
    }

    override fun findAll(): List<Exclusion> {
        val exclusions =
            handle.createQuery(
                """
                SELECT * FROM ps.EXCEPTION_APP
                """.trimIndent(),
            )
                .mapTo(AppExclusion::class.java)
                .list()
        val contactExclusions =
            handle.createQuery(
                """
                SELECT * FROM ps.EXCEPTION_CONTACT
                """.trimIndent(),
            )
                .mapTo(ContactExclusion::class.java)
                .list()
        return exclusions + contactExclusions
    }

    override fun findAllAppExclusions(): List<AppExclusion> {
        return handle.createQuery("SELECT * FROM ps.EXCEPTION_APP")
            .mapTo(AppExclusion::class.java)
            .list()
    }

    override fun findAllContactExclusions(): List<ContactExclusion> {
        return handle.createQuery("SELECT * FROM ps.EXCEPTION_CONTACT")
            .mapTo(ContactExclusion::class.java)
            .list()
    }

    override fun findByIdAppExclusions(id: Int): AppExclusion? {
        return handle.createQuery("SELECT * FROM ps.EXCEPTION_APP WHERE id = :id")
            .bind("id", id)
            .mapTo(AppExclusion::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findByIdContactExclusions(id: Int): ContactExclusion? {
        return handle.createQuery("SELECT * FROM ps.EXCEPTION_CONTACT WHERE id = :id")
            .bind("id", id)
            .mapTo(ContactExclusion::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findAppExclusionsByUserId(user: User): List<AppExclusion> {
        return handle.createQuery("SELECT * FROM ps.EXCEPTION_APP WHERE user_id = :userId")
            .bind("userId", user.id)
            .mapTo(AppExclusion::class.java)
            .list()
    }

    // TODO Improve
    override fun findExclusionsByRuleId(rule: Rule): List<Exclusion> {
        return if (rule is RuleEvent) {
            val appExclusions =
                handle.createQuery(
                    """
                    SELECT * FROM ps.EXCEPTION_APP WHERE id IN (
                        SELECT exception_id FROM ps.EXCEPTION_APP_RULE_EVENT WHERE rule_id = :ruleId
                    )
                    """.trimIndent(),
                )
                    .bind("ruleId", rule.id)
                    .mapTo(AppExclusion::class.java)
                    .list()
            val contactExclusions =
                handle.createQuery(
                    """
                    SELECT * FROM ps.EXCEPTION_CONTACT WHERE id IN (
                        SELECT exception_id FROM ps.EXCEPTION_CONTACT_RULE_EVENT WHERE rule_id = :ruleId
                    )
                    """.trimIndent(),
                )
                    .bind("ruleId", rule.id)
                    .mapTo(ContactExclusion::class.java)
                    .list()
            appExclusions + contactExclusions
        } else {
            val appExclusions =
                handle.createQuery(
                    """
                    SELECT * FROM ps.EXCEPTION_APP WHERE id IN(
                        SELECT exception_id FROM ps.EXCEPTION_APP_RULE_LOCATION WHERE rule_id = :ruleId
                    )
                    """.trimIndent(),
                )
                    .bind("ruleId", rule.id)
                    .mapTo(AppExclusion::class.java)
                    .list()
            val contactExclusions =
                handle.createQuery(
                    """
                    SELECT * FROM ps.EXCEPTION_CONTACT WHERE id IN(
                        SELECT exception_id FROM ps.EXCEPTION_CONTACT_RULE_LOCATION WHERE rule_id = :ruleId
                    )
                    """.trimIndent(),
                )
                    .bind("ruleId", rule.id)
                    .mapTo(ContactExclusion::class.java)
                    .list()
            appExclusions + contactExclusions
        }
    }

    override fun findContactExclusionsByUserId(user: User): List<ContactExclusion> {
        return handle.createQuery("SELECT * FROM ps.EXCEPTION_CONTACT WHERE user_id = :userId")
            .bind("userId", user.id)
            .mapTo(ContactExclusion::class.java)
            .list()
    }

    override fun findAllExclusionsByUserId(user: User): List<Exclusion> {
        val appExclusions =
            handle.createQuery("SELECT * FROM ps.EXCEPTION_APP WHERE user_id = :userId")
                .bind("userId", user.id)
                .mapTo(AppExclusion::class.java)
                .list()
        val contactExclusions =
            handle.createQuery("SELECT * FROM ps.EXCEPTION_CONTACT WHERE user_id = :userId")
                .bind("userId", user.id)
                .mapTo(ContactExclusion::class.java)
                .list()
        return appExclusions + contactExclusions
    }

    override fun addAppExclusionToRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeAppExclusionFromRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun removeContactExclusionFromRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun addContactExclusionToRule(
        ruleId: Int,
        exclusionId: Int,
    ): Boolean {
        TODO("Not yet implemented")
    }

    override fun updateAppExclusion(
        appExclusion: AppExclusion,
        appName: String,
    ): AppExclusion {
        handle.createUpdate(
            """
            UPDATE ps.EXCEPTION_APP SET app_name = :appName WHERE id = :id
            """.trimIndent(),
        )
            .bind("appName", appName)
            .bind("id", appExclusion.id)
            .execute()
        return appExclusion.copy(name = appName)
    }

    override fun updateContactExclusion(
        contactExclusion: ContactExclusion,
        contactName: String,
        phoneNumber: String,
    ): ContactExclusion {
        handle.createUpdate(
            """
            UPDATE ps.EXCEPTION_CONTACT SET name = :contactName, phone_number = :phoneNumber WHERE id = :id
            """.trimIndent(),
        )
            .bind("contactName", contactName)
            .bind("phoneNumber", phoneNumber)
            .bind("id", contactExclusion.id)
            .execute()
        return contactExclusion.copy(name = contactName, phoneNumber = phoneNumber)
    }

    override fun deleteAppExclusion(appExclusion: AppExclusion): Boolean {
        return handle.createUpdate(
            """
            DELETE FROM ps.EXCEPTION_APP WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", appExclusion.id)
            .execute() > 0
    }

    override fun deleteContactExclusion(contactExclusion: ContactExclusion): Boolean {
        return handle.createUpdate(
            """
            )
            DELETE FROM ps.EXCEPTION_CONTACT WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", contactExclusion.id)
            .execute() > 0
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM ps.EXCEPTION_APP")
            .execute()
        handle.createUpdate("DELETE FROM ps.EXCEPTION_CONTACT")
            .execute()
    }

    override fun addAppExclusionToRuleEvent(
        rule: RuleEvent,
        exclusion: AppExclusion,
    ): Boolean {
        return handle.createUpdate(
            """
            INSERT INTO ps.EXCEPTION_APP_RULE_EVENT (exception_id, rule_id)
            VALUES (:exceptionId, :ruleId)
            """.trimIndent(),
        )
            .bind("exceptionId", exclusion.id)
            .bind("ruleId", rule.id)
            .execute() > 0
    }

    override fun addAppExclusionToRuleLocation(
        rule: RuleLocation,
        exclusion: AppExclusion,
    ): Boolean {
        return handle.createUpdate(
            """
            INSERT INTO ps.EXCEPTION_APP_RULE_LOCATION (exception_id, rule_id)
            VALUES (:exceptionId, :ruleId)
            """.trimIndent(),
        )
            .bind("exceptionId", exclusion.id)
            .bind("ruleId", rule.id)
            .execute() > 0
    }

    override fun addContactExclusionToRuleEvent(
        rule: RuleEvent,
        exclusion: ContactExclusion,
    ): Boolean {
        return handle.createUpdate(
            """
            INSERT INTO ps.EXCEPTION_CONTACT_RULE_EVENT (exception_id, rule_id)
            VALUES (:exceptionId, :ruleId)
            """.trimIndent(),
        )
            .bind("exceptionId", exclusion.id)
            .bind("ruleId", rule.id)
            .execute() > 0
    }

    override fun addContactExclusionToRuleLocation(
        rule: RuleLocation,
        exclusion: ContactExclusion,
    ): Boolean {
        return handle.createUpdate(
            """
            INSERT  INTO ps.EXCEPTION_CONTACT_RULE_LOCATION (exception_id, rule_id)
            VALUES (:exceptionId, :ruleId)
            """.trimIndent(),
        )
            .bind("exceptionId", exclusion.id)
            .bind("ruleId", rule.id)
            .execute() > 0
    }
}
