package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Event
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * EventRepositoryInterface defines the contract for managing events in the application.
 * It provides methods to fetch, update, delete, and insert events, as well as synchronize them with a remote source.
 */
interface EventRepositoryInterface {
    /**
     * Fetches a flow of events.
     * @return Either an ApiError or a Flow of List of Event.
     */
    suspend fun fetchEvents(): Either<ApiError, Flow<List<Event>>>

    /**
     * Retrieves a event by its ID.
     * @return Either a [Event] or null if not found.
     */
    suspend fun getEventById(id: Int): Event?

    /**
     * Retrieves
     * @return Either a [Event] or null if not found.
     */
    suspend fun getByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Event?

    /**
     * Updates an existing event.
     * @param event The event to be updated.
     * @return Either an ApiError or the updated [Event].
     */
    suspend fun updateEvent(event: Event): Either<ApiError, Event>

    /**
     * Deletes an event.
     * @param event The event to be deleted.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteEvent(event: Event): Either<ApiError, Unit>

    /**
     * Clears all events from the repository.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun clear()

    /**
     * Inserts a new event into the repository.
     * @param calendarId The ID of the calendar to which the event belongs.
     * @param eventId The ID of the event.
     * @param title The title of the event.
     * @param startTime The start time of the event.
     * @param endTime The end time of the event.
     * @return Either an ApiError or the inserted [Event].
     */
    suspend fun insertEvent(
        calendarId: Long,
        eventId: Long,
        title: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Either<ApiError, Event>

    /**
     * Synchronizes events with a remote source.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun syncEvents(): Either<ApiError, Unit>
}
