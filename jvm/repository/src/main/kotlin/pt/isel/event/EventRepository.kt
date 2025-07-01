package pt.isel.event

import kotlinx.datetime.LocalDateTime
import pt.isel.Event
import pt.isel.User

/**
 * Interface that defines the operations that can be done on the Event repository.
 */
interface EventRepository {
    fun create(
        title: String,
        user: User,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Event

    fun findById(id: Int): Event?

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
