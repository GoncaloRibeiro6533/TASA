package pt.isel

import org.jdbi.v3.core.Handle
import pt.isel.rule.RuleRepository
import java.time.LocalDateTime

class JdbiRuleRepository(
    private val handle: Handle,
) : RuleRepository {
    override fun createEventRule(
        event: Event,
        user: User,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): RuleEvent {
        val id =
            handle.createUpdate(
                """
                INSERT INTO ps.RULE_EVENT (start_time, end_time, user_id, event_id)
                VALUES (:startTime, :endTime, :userId, :eventId)
                """.trimIndent(),
            )
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("userId", user.id)
                .bind("eventId", event.id)
                .executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java).one()
        return RuleEvent(
            id = id,
            startTime = startTime,
            endTime = endTime,
            creator = user,
            event = event,
        )
    }

    override fun createLocationRule(
        location: Location,
        user: User,
    ): RuleLocation {
        val id =
            handle.createUpdate(
                """
                INSERT INTO ps.RULE_LOCATION (user_id, location_id)
                VALUES (:userId, :locationId)
                """.trimIndent(),
            )
                .bind("userId", user.id)
                .bind("locationId", location.id)
                .executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java).one()
        return RuleLocation(
            id = id,
            creator = user,
            location = location,
        )
    }

    override fun findAll(): List<Rule> {
        val locationRules =
            handle.createQuery(
                """
                SELECT
                    r.id,
                    r.user_id,
                    r.location_id,
                    u.username,
                    u.email,
                    l.name,
                    l.latitude,
                    l.longitude,
                    l.radius
                FROM ps.RULE_LOCATION r
                JOIN ps.USER u ON r.user_id = u.id
                JOIN ps.LOCATION l ON r.location_id = l.id AND r.user_id = l.user_id;
                """.trimIndent(),
            ).mapTo(RuleLocation::class.java)
                .list()
        val eventRules =
            handle.createQuery(
                """
                SELECT
                    r.id,
                    r.start_time,
                    r.end_time,
                    r.user_id,
                    r.event_id,
                    e.title,
                    e.start_time as event_start_time,
                    e.end_time as event_end_time,
                    u.username,
                    u.email
                FROM ps.RULE_EVENT r
                JOIN ps.USER u ON r.user_id = u.id
                JOIN ps.EVENT e ON e.id = r.event_id;
                """.trimIndent(),
            ).mapTo(RuleEvent::class.java)
                .list()
        return locationRules + eventRules
    }

    override fun findRuleEventById(id: Int): RuleEvent? {
        return handle.createQuery(
            """
            SELECT
                r.id,
                r.start_time,
                r.end_time,
                r.user_id,
                r.event_id,
                e.title,
                e.start_time as event_start_time,
                e.end_time as event_end_time,
                u.username,
                u.email
            FROM ps.RULE_EVENT r
            JOIN ps.USER u ON r.user_id = u.id
            JOIN ps.EVENT e ON e.id = r.event_id 
            WHERE r.id= :id
            """.trimIndent(),
        )
            .bind("id", id)
            .mapTo(RuleEvent::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findRuleLocationById(id: Int): RuleLocation? {
        return handle.createQuery(
            """
            SELECT
                r.id,
                r.user_id,
                r.location_id,
                u.username,
                u.email,
                l.name,
                l.latitude,
                l.longitude,
                l.radius
            FROM ps.RULE_LOCATION r
            JOIN ps.USER u ON r.user_id = u.id
            JOIN ps.LOCATION l ON r.location_id = l.id AND r.user_id = l.user_id
            WHERE r.id= :id
            """.trimIndent(),
        )
            .bind("id", id)
            .mapTo(RuleLocation::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findByUserId(user: User): List<Rule> {
        val locationRules =
            handle.createQuery(
                """
                SELECT
                    r.id,
                    r.user_id,
                    r.location_id,
                    u.username,
                    u.email,
                    l.name,
                    l.latitude,
                    l.longitude,
                    l.radius
                FROM ps.RULE_LOCATION r
                JOIN ps.USER u ON r.user_id = u.id
                JOIN ps.LOCATION l ON r.location_id = l.id AND r.user_id = l.user_id
                WHERE r.user_id= :userId
                """.trimIndent(),
            )
                .bind("userId", user.id)
                .mapTo(RuleLocation::class.java)
                .list()
        val eventRules =
            handle.createQuery(
                """
                SELECT
                    r.id,
                    r.start_time,
                    r.end_time,
                    r.user_id,
                    r.event_id,
                    e.title,
                    e.start_time as event_start_time,
                    e.end_time as event_end_time,
                    u.username,
                    u.email
                FROM ps.RULE_EVENT r
                JOIN ps.USER u ON r.user_id = u.id
                JOIN ps.EVENT e ON e.id = r.event_id 
                WHERE r.user_id= :userId
                """.trimIndent(),
            )
                .bind("userId", user.id)
                .mapTo(RuleEvent::class.java)
                .list()
        return locationRules + eventRules
    }

    override fun updateRuleEvent(
        rule: RuleEvent,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): RuleEvent {
        handle.createUpdate(
            """
            UPDATE ps.RULE_EVENT
            SET start_time = :startTime, end_time = :endTime
            WHERE id = :id
            """.trimIndent(),
        )
            .bind("startTime", startTime)
            .bind("endTime", endTime)
            .bind("id", rule.id)
        return rule.copy(
            startTime = startTime,
            endTime = endTime,
        )
    }

    override fun deleteRuleEvent(rule: RuleEvent): Boolean {
        return handle.createUpdate("DELETE FROM ps.RULE_EVENT WHERE id = :id")
            .bind("id", rule.id)
            .execute() > 0
    }

    override fun deleteLocationEvent(rule: RuleLocation): Boolean {
        return handle.createUpdate("DELETE FROM ps.RULE_LOCATION WHERE id = :id")
            .bind("id", rule.id)
            .execute() > 0
    }

    override fun clear() {
        handle.execute("DELETE FROM ps.RULE_EVENT")
        handle.execute("DELETE FROM ps.RULE_LOCATION")
    }
}
