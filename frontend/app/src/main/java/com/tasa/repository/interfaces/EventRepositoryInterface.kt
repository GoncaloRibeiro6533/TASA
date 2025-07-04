package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.storage.entities.EventEntity
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow

interface EventRepositoryInterface {
    suspend fun fetchEvents(): Either<ApiError, Flow<List<EventEntity>>>

    suspend fun fetchEventsByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Flow<Event?>

    suspend fun updateEvent(event: Event): Either<ApiError, Event>

    suspend fun deleteEvent(event: Event): Either<ApiError, Unit>

    suspend fun clear()
}
