package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface EventRepositoryInterface {
    suspend fun fetchEvents(): Either<ApiError, Flow<List<Event>>>

    suspend fun getEventById(id: Int): Event?

    suspend fun getByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Event?

    suspend fun updateEvent(event: Event): Either<ApiError, Event>

    suspend fun deleteEvent(event: Event): Either<ApiError, Unit>

    suspend fun clear()

    suspend fun syncEvents(): Either<ApiError, Unit>

    suspend fun insertEvent(
        calendarId: Long,
        eventId: Long,
        title: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Either<ApiError, Event>
}
