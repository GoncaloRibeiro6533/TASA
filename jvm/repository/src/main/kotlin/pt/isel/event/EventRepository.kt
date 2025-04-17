package pt.isel.event

import pt.isel.Event
import pt.isel.User

/**
 * Interface that defines the operations that can be done on the Event repository.
 */
interface EventRepository {
    fun create(
        eventId: Long,
        calendarId: Long,
        title: String,
        user: User,
    ): Event

    fun findById(
        eventId: Long,
        calendarId: Long,
        user: User,
    ): Event?

    fun findAll(): List<Event>

    fun findByUserId(user: User): List<Event>

    fun update(
        user: User,
        event: Event,
        newTitle: String,
    ): Event

    fun delete(
        user: User,
        event: Event,
    ): Boolean

    fun clear()
}
