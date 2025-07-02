package com.tasa.service.http

import com.tasa.domain.ApiError
import com.tasa.domain.Event
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
    override suspend fun fetchEvents(): Either<ApiError, List<Event>> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Event?> {
        return when (val response = client.get<Event>("/event/$id/calendar/$calendarId")) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchEventAll(): Either<ApiError, List<EventOutput>> {
        return when (val response = client.get<List<EventOutput>>("/event/all")) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertEvent(event: Event): Either<ApiError, Event> {
        return when (val response = client.post<Event>("/event/create", body = event)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertEvents(events: List<Event>): Either<ApiError, List<Event>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateEventTitle(event: Event): Either<ApiError, Event> {
        return when (val response = client.put<Event>("/event/update/title", body = event)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun deleteEventById(
        id: Long,
        calendarId: Long,
    ): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/event/remove/$id/calendar/$calendarId")) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }
}
