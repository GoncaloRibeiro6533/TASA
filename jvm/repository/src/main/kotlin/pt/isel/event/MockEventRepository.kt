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
        user: User,
        event: Event,
        newTitle: String,
    ): Event {
        events[user.id]?.remove(event)
        val newEvent = event.copy(title = newTitle)
        events[user.id]?.add(newEvent)
        return newEvent
    }

    override fun delete(
        user: User,
        event: Event,
    ): Boolean {
        return events[user.id]?.remove(event) == true
    }

    override fun clear() {
        events.clear()
    }
}
