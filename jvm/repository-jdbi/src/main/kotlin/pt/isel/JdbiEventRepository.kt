package pt.isel

import org.jdbi.v3.core.Handle
import pt.isel.event.EventRepository
import java.time.LocalDateTime

class JdbiEventRepository(
    private val handle: Handle,
) : EventRepository {
    override fun create(
        title: String,
        user: User,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Event {
        val id =
            handle.createUpdate(
                """
                INSERT INTO ps.Event (title, user_id, start_time, end_time)
                VALUES (:title, :userId, :startTime, :endTime)
                """.trimIndent(),
            )
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .bind("title", title)
                .bind("userId", user.id).executeAndReturnGeneratedKeys()
                .mapTo(Int::class.java).one()
        return Event(
            id = id,
            title = title,
            startTime = startTime,
            endTime = endTime,
        )
    }

    override fun findById(id: Int): Event? {
        return handle.createQuery(
            """
            SELECT id,title, start_time, end_time FROM ps.event 
            WHERE id = :eventId
            """.trimIndent(),
        )
            .bind("eventId", id)
            .mapTo(Event::class.java)
            .findOne()
            .orElse(null)
    }

    override fun findAll(): List<Event> {
        return handle.createQuery("SELECT * FROM ps.event")
            .mapTo(Event::class.java)
            .list()
    }

    override fun findByUserId(user: User): List<Event> {
        return handle.createQuery("SELECT id, title, start_time, end_time FROM ps.event WHERE user_id = :userId")
            .bind("userId", user.id)
            .mapTo(Event::class.java)
            .list()
    }

    override fun update(
        user: User,
        event: Event,
        newTitle: String,
    ): Event {
        handle.createUpdate(
            """
            UPDATE ps.event SET title = :newTitle 
                WHERE id = :eventId
            """.trimIndent(),
        )
            .bind("newTitle", newTitle)
            .bind("eventId", event.id)
            .execute()
        return event.copy(title = newTitle)
    }

    override fun delete(
        user: User,
        event: Event,
    ): Boolean {
        return handle.createUpdate("DELETE FROM ps.event WHERE id = :eventId")
            .bind("eventId", event.id)
            .execute() > 0
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM ps.event")
            .execute()
    }
}
