package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.service.http.models.event.EventOutput
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
                    eventId = 1,
                    calendarId = 1,
                    title = "Event 1",
                ),
            )
    }

    override suspend fun fetchEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Event?> {
        return success(null)
    }

    override suspend fun fetchEventAll(token: String): Either<ApiError, List<EventOutput>> {
        return success(emptyList())
    }

    override suspend fun insertEvent(
        event: Event,
        token: String,
    ): Either<ApiError, Event> {
        events.add(event)
        return success(event)
    }

    override suspend fun updateEventTitle(
        event: Event,
        token: String,
    ): Either<ApiError, Event> {
        if (!events.contains(event)) return failure(ApiError("Event not found"))
        events.removeIf { it.id == event.id && it.calendarId == event.calendarId }
        events.add(event)
        return success(event)
    }

    override suspend fun deleteEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        return success(Unit)
    }
}
