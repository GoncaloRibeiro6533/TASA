package com.tasa.service.http

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.service.http.models.event.EventInput
import com.tasa.service.http.models.event.EventOutput
import com.tasa.service.http.utils.delete
import com.tasa.service.http.utils.get
import com.tasa.service.http.utils.post
import com.tasa.service.http.utils.put
import com.tasa.service.interfaces.EventService
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import io.ktor.client.HttpClient

class EventServiceHttp(private val client: HttpClient) : EventService {
    override suspend fun fetchEventById(
        id: Int,
        token: String,
    ): Either<ApiError, EventOutput?> {
        return when (val response = client.get<EventOutput>("/event/$id", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchEventAll(token: String): Either<ApiError, List<EventOutput>> {
        return when (val response = client.get<List<EventOutput>>("/event/all", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertEvent(
        event: EventInput,
        token: String,
    ): Either<ApiError, EventOutput> {
        return when (val response = client.post<EventOutput>("/event/create", body = event, token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun updateEventTitle(
        event: Event,
        token: String,
    ): Either<ApiError, Event> {
        return when (val response = client.put<EventOutput>("/event/update/title", body = event, token = token)) {
            is Success -> success(response.value.toEvent(event.eventId, event.calendarId))
            is Failure -> failure(response.value)
        }
    }

    override suspend fun deleteEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/event/remove/$id", token = token)) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }
}
