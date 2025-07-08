package pt.isel

import jakarta.inject.Named
import pt.isel.transaction.TransactionManager
import java.time.LocalDateTime

/**
 * Represents the possible errors that can occur when creating, updating, deleting or retrieving events.
 */
sealed class EventError {
    data object EventNotFound : EventError()

    data object NegativeIdentifier : EventError()

    data object EventNameCannotBeBlank : EventError()

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
 * @property trxManager The transaction manager used to execute operations within a transactional context.
 */
@Named
class EventService(
    private val trxManager: TransactionManager,
) {
    /**
     * Creates an event for a user.
     *.
     * @param title The title of the event.
     * @param userId The ID of the user creating the event.
     * @param startTime The start time of the event.
     * @endTime The end time of the event.
     *
     * @return Either an [EventError] or the created [Event].
     */
    fun createEvent(
        title: String,
        userId: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Either<EventError, Event> {
        return trxManager.run {
            if (title.isBlank()) {
                return@run failure(EventError.EventNameCannotBeBlank)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val events = eventRepo.findByUserId(user)
            if (events.any { it.title == title && it.startTime == startTime && it.endTime == endTime }) {
                return@run failure(EventError.AlreadyExists)
            }
            return@run success(
                eventRepo.create(
                    title = title,
                    user = user,
                    startTime = startTime,
                    endTime = endTime,
                ),
            )
        }
    }

    /**
     * Updates the title of an event.
     *
     * @param eventId The ID of the event to be updated.
     * @param newTitle The new title for the event.
     * @param userId The ID of the user updating the event.
     *
     * @return Either an [EventError] or the updated [Event].
     */
    fun updateEvent(
        eventId: Int,
        newTitle: String,
        userId: Int,
    ): Either<EventError, Event> {
        return trxManager.run {
            if (eventId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (newTitle.isBlank()) {
                return@run failure(EventError.EventNameCannotBeBlank)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val event = eventRepo.findById(eventId) ?: return@run failure(EventError.EventNotFound)
            val events = eventRepo.findByUserId(user)
            if (events.any { it.title == newTitle && it.startTime == event.startTime && it.endTime == event.endTime }) {
                return@run failure(EventError.AlreadyExists)
            }
            if (event !in eventRepo.findByUserId(user)) return@run failure(EventError.NotAllowed)
            eventRepo.update(user, event, newTitle)
            return@run success(event.copy(title = newTitle))
        }
    }

    fun getEvent(
        eventId: Int,
        userId: Int,
    ): Either<EventError, Event> =
        trxManager.run {
            if (eventId < 0 || userId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val event = eventRepo.findById(eventId) ?: return@run failure(EventError.EventNotFound)
            if (event !in eventRepo.findByUserId(user)) return@run failure(EventError.NotAllowed)
            return@run success(event)
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
     *
     * @return Either an [EventError] or a boolean indicating success.
     */
    fun deleteEvent(
        userId: Int,
        eventId: Int,
    ): Either<EventError, Boolean> {
        return trxManager.run {
            if (eventId < 0 || userId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val event = eventRepo.findById(eventId) ?: return@run failure(EventError.EventNotFound)
            if (event !in eventRepo.findByUserId(user)) return@run failure(EventError.NotAllowed)
            val rules = ruleRepo.findByUserId(user)
            if (rules.filterIsInstance<RuleEvent>().any { it.event.id == event.id }) {
                return@run failure(EventError.NotAllowed)
            }
            return@run success(eventRepo.delete(user, event))
        }
    }
}
