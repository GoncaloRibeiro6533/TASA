package pt.isel

import org.jdbi.v3.core.Handle
import pt.isel.event.EventRepository

class JdbiEventRepository(
    private val handle: Handle,
) : EventRepository {
    override fun create(
        eventId: Long,
        calendarId: Long,
        title: String,
        user: User,
    ): Event {
        handle.createUpdate(
            """
            INSERT INTO ps.Event (event_id, calendar_id, title, user_id) 
            VALUES (:eventId, :calendarId, :title, :userId)
            """.trimIndent(),
        )
            .bind("eventId", eventId)
            .bind("calendarId", calendarId)
            .bind("title", title)
            .bind("userId", user.id)
            .execute()
        return Event(
            id = eventId,
            calendarId = calendarId,
            title = title,
        )
    }

    override fun findById(
        eventId: Long,
        calendarId: Long,
        user: User,
    ): Event? {
        return handle.createQuery(
            """
            SELECT event_id, calendar_id,title FROM ps.event 
            WHERE event_id = :eventId AND calendar_id = :calendarId AND user_id = :userId  
            """.trimIndent(),
        )
            .bind("eventId", eventId)
            .bind("calendarId", calendarId)
            .bind("userId", user.id)
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
        return handle.createQuery("SELECT event_id, calendar_id, title FROM ps.event WHERE user_id = :userId")
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
                WHERE event_id = :eventId AND calendar_id = :calendarId AND user_id = :userId
            """.trimIndent(),
        )
            .bind("newTitle", newTitle)
            .bind("eventId", event.id)
            .bind("calendarId", event.calendarId)
            .bind("userId", user.id)
            .execute()
        return event.copy(title = newTitle)
    }

    override fun delete(
        user: User,
        event: Event,
    ): Boolean {
        return handle.createUpdate("DELETE FROM ps.event WHERE event_id = :eventId AND calendar_id = :calendarId AND user_id = :userId")
            .bind("eventId", event.id)
            .bind("calendarId", event.calendarId)
            .bind("userId", user.id)
            .execute() > 0
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM ps.event")
            .execute()
    }
}
