package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.service.interfaces.EventService
import com.tasa.utils.Either
import com.tasa.utils.failure
import com.tasa.utils.success

class EventServiceFake : EventService {
    companion object {
        private val events =
            mutableListOf<Event>(
                Event(
                    id = 1,
                    calendarId = 1,
                    title = "Event 1",
                ),
            )
    }

    override suspend fun fetchEvents(): Either<ApiError, List<Event>> {
        return success(events)
    }

    override suspend fun fetchEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Event?> {
        return success(events.find { it.id == id })
    }

    override suspend fun fetchEventAll(): Either<ApiError, List<Event>> {
        return success(events)
    }

    override suspend fun insertEvent(event: Event): Either<ApiError, Event> {
        events.add(event)
        return success(event)
    }

    override suspend fun insertEvents(events: List<Event>): Either<ApiError, List<Event>> {
        Companion.events.addAll(events)
        return success(events)
    }

    override suspend fun updateEventTitle(event: Event): Either<ApiError, Event> {
        if (!events.contains(event)) return failure(ApiError("Event not found"))
        events.removeIf { it.id == event.id && it.calendarId == event.calendarId }
        events.add(event)
        return success(event)
    }

    override suspend fun deleteEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Unit> {
        if (!events.any { it.id == id && it.calendarId == calendarId }) return failure(ApiError("Event not found"))
        events.removeIf { it.id == id && it.calendarId == calendarId }
        return success(Unit)
    }
}
