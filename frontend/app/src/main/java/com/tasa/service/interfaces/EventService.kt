package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.service.http.models.event.EventInput
import com.tasa.service.http.models.event.EventOutput
import com.tasa.utils.Either

/**
 * EventService defines the contract for managing events in the application.
 * It provides methods to fetch, update, delete, and insert events.
 */
interface EventService {
    /**
     * Fetches a event by its ID.
     * @param id The ID of the event to be retrieved.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the retrieved EventOutput.
     */
    suspend fun fetchEventById(
        id: Int,
        token: String,
    ): Either<ApiError, EventOutput?>

    /**
     * Fetches all the events of the user.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the retrieved events.
     */
    suspend fun fetchEventAll(token: String): Either<ApiError, List<EventOutput>>

    /**
     * Inserts a new event with the specified parameters.
     * @param event The event to be inserted.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the inserted EventOutput.
     */
    suspend fun insertEvent(
        event: EventInput,
        token: String,
    ): Either<ApiError, EventOutput>

    /**
     * Updates an existing event.
     * @param event The event to be updated.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the updated EventOutput.
     */
    suspend fun updateEventTitle(
        event: Event,
        token: String,
    ): Either<ApiError, Event>

    /**
     * Deletes an event by its ID.
     * @param id The ID of the event to be deleted.
     * @param token The authentication token for the request.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteEventById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>
}
