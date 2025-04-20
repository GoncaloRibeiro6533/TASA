package pt.isel

import jakarta.inject.Named
import pt.isel.transaction.TransactionManager

/**
 * Represents the possible errors that can occur when creating, updating, deleting or retrieving events.
 */
sealed class EventError {
    data object EventNotFound : EventError()

    data object NegativeIdentifier : EventError()

    data object InvalidEventName : EventError()

    data object InvalidEventDate : EventError()

    data object InvalidEventLocation : EventError()

    data object NotAllowed : EventError()

    data object AlreadyExists : EventError()

    data object UserNotFound : EventError()
}

/**
 * Service that manages events of users. This includes operations
 * for managing contact and app exclusions, such as creation, retrieval,
 * deletion, and updating of events.
 *
 * @constructor Initializes the `EventService` with the provided `TransactionManager`.
 *
 * @param trxManager The transaction manager used to execute operations within a transactional context.
 */
@Named
class EventService(
    private val trxManager: TransactionManager,
) {
    /**
     * Creates an event for a user.
     *
     * @param eventId The ID of the event to be created.
     * @param calendarId The ID of the calendar to which the event belongs.
     * @param title The title of the event.
     * @param userId The ID of the user creating the event.
     *
     * @return Either an [EventError] or the created [Event].
     */
    fun createEvent(
        eventId: Long,
        calendarId: Long,
        title: String,
        userId: Int,
    ): Either<EventError, Event> {
        return trxManager.run {
            if (eventId < 0 || calendarId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (title.isBlank()) {
                return@run failure(EventError.InvalidEventName)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            if (eventRepo.findByUserId(user)
                    .any { it.id == eventId && it.calendarId == calendarId }
            ) {
                return@run failure(EventError.AlreadyExists)
            }
            return@run success(
                eventRepo.create(
                    eventId = eventId,
                    calendarId = calendarId,
                    title = title,
                    user = user,
                ),
            )
        }
    }

    /**
     * Updates the title of an event.
     *
     * @param eventId The ID of the event to be updated.
     * @param calendarId The ID of the calendar to which the event belongs.
     * @param newTitle The new title for the event.
     * @param userId The ID of the user updating the event.
     *
     * @return Either an [EventError] or the updated [Event].
     */
    fun updateEvent(
        eventId: Long,
        calendarId: Long,
        newTitle: String,
        userId: Int,
    ): Either<EventError, Event> {
        return trxManager.run {
            if (eventId < 0 || calendarId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (newTitle.isBlank()) {
                return@run failure(EventError.InvalidEventName)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val event = eventRepo.findById(eventId, calendarId, user) ?: return@run failure(EventError.EventNotFound)
            if (event !in eventRepo.findByUserId(user)) return@run failure(EventError.NotAllowed)
            eventRepo.update(user, event, newTitle)
            return@run success(event.copy(title = newTitle))
        }
    }

    /**
     * Retrieves all events of a user.
     *
     * @param userId The ID of the user whose events are to be retrieved.
     *
     * @return Either an [EventError] or a list of [Event]s.
     */
    fun getEventsOfUser(userId: Int): Either<EventError, List<Event>> {
        return trxManager.run {
            if (userId < 0) return@run failure(EventError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            return@run success(eventRepo.findByUserId(user))
        }
    }

    /**
     * Deletes an event of a user.
     *
     * @param userId The ID of the user whose events are to be retrieved.
     * @param eventId The ID of the event to be deleted.
     * @param calendarId The ID of the calendar whose events are to be retrieved.
     *
     * @return Either an [EventError] or a boolean indicating success.
     */
    fun deleteEvent(
        userId: Int,
        eventId: Long,
        calendarId: Long,
    ): Either<EventError, Boolean> {
        return trxManager.run {
            if (eventId < 0 || calendarId < 0 || userId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val event = eventRepo.findById(eventId, calendarId, user) ?: return@run failure(EventError.EventNotFound)
            if (event !in eventRepo.findByUserId(user)) return@run failure(EventError.NotAllowed)
            return@run success(eventRepo.delete(user, event))
        }
    }
}
