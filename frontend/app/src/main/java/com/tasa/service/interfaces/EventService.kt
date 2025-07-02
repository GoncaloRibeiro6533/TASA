package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.service.http.models.event.EventOutput
import com.tasa.utils.Either

interface EventService {
    suspend fun fetchEvents(): Either<ApiError, List<Event>>

    suspend fun fetchEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Event?>

    suspend fun fetchEventAll(): Either<ApiError, List<EventOutput>>

    suspend fun insertEvent(event: Event): Either<ApiError, Event>

    suspend fun insertEvents(events: List<Event>): Either<ApiError, List<Event>>

    suspend fun updateEventTitle(event: Event): Either<ApiError, Event>

    suspend fun deleteEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Unit>
}
