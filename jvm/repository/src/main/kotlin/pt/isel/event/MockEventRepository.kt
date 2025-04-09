package pt.isel.event

import pt.isel.Event
import pt.isel.User

class MockEventRepository : EventRepository {
    private val events = mutableMapOf<Int, MutableList<Event>>()

    override fun create(
        eventId: Long,
        calendarId: Long,
        title: String,
        user: User,
    ): Event {
        val event =
            Event(
                id = eventId,
                calendarId = calendarId,
                title = title,
            )
        events.computeIfAbsent(user.id) { mutableListOf() }.add(event)
        return event
    }

    override fun findById(
        eventId: Long,
        calendarId: Long,
        user: User,
    ): Event? = events[user.id]?.find { it.id == eventId && it.calendarId == calendarId }

    override fun findAll(): List<Event> = events.values.flatten()

    override fun findByUserId(user: User): List<Event> = events[user.id]?.toList() ?: emptyList()

    override fun update(
        event: Event,
        user: User,
    ): Event {
        events[user.id]?.remove(event)
        events[user.id]?.add(event)
        return event
    }

    override fun delete(
        event: Event,
        user: User,
    ): Boolean {
        return events[user.id]?.remove(event) == true
    }

    override fun clear() {
        events.clear()
    }
}
