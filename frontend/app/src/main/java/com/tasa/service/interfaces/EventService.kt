package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.service.http.models.event.EventOutput
import com.tasa.utils.Either

interface EventService {
    suspend fun fetchEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Event?>

    suspend fun fetchEventAll(token: String): Either<ApiError, List<EventOutput>>

    suspend fun insertEvent(
        event: Event,
        token: String,
    ): Either<ApiError, Event>

    suspend fun updateEventTitle(
        event: Event,
        token: String,
    ): Either<ApiError, Event>

    suspend fun deleteEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>
}
