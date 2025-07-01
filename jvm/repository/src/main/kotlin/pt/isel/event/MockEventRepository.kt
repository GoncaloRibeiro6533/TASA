package pt.isel.event

import kotlinx.datetime.LocalDateTime
import pt.isel.Event
import pt.isel.User

class MockEventRepository : EventRepository {
    companion object {
        private var eventId = 0
        private val events = mutableMapOf<Int, MutableList<Event>>()
    }

    override fun create(
        title: String,
        user: User,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Event {
        val event =
            Event(
                id = eventId++,
                title = title,
                startTime = startTime,
                endTime = endTime,
            )
        events.computeIfAbsent(user.id) { mutableListOf() }.add(event)
        return event
    }

    override fun findById(id: Int): Event? =
        events.values
            .flatten()
            .find { it.id == id }

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
        eventId = 0
    }
}
