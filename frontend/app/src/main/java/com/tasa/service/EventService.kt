package com.tasa.service

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.utils.Either

interface EventService {
    suspend fun fetchEvents(): Either<ApiError, List<Event>>

    suspend fun fetchEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Event?>

    suspend fun fetchEventByName(name: String): Either<ApiError, Event?>

    suspend fun insertEvent(event: Event): Either<ApiError, Event>

    suspend fun insertEvents(events: List<Event>): Either<ApiError, List<Event>>

    suspend fun updateEvent(event: Event): Either<ApiError, Event>

    suspend fun deleteEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Unit>

    suspend fun clear(): Either<ApiError, Unit>
}
