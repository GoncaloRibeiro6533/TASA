package pt.isel

import jakarta.inject.Named
import pt.isel.transaction.TransactionManager

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

@Named
class EventService(
    private val trxManager: TransactionManager,
) {
    fun createEvent(
        eventId: Long,
        calendarId: Long,
        title: String,
        userId: Int,
    ): Either<EventError, Event> {
        return trxManager.run {
            if (eventId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (calendarId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (title.isBlank()) {
                return@run failure(EventError.InvalidEventName)
            }
            if (eventRepo.findByUserId(userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound))
                    .any { it.id == eventId && it.calendarId == calendarId }
            ) {
                return@run failure(EventError.AlreadyExists)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
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

    fun updateEvent(
        eventId: Long,
        calendarId: Long,
        newTitle: String,
        userId: Int,
    ): Either<EventError, Event> {
        return trxManager.run {
            if (eventId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (calendarId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (newTitle.isBlank()) {
                return@run failure(EventError.InvalidEventName)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val event = eventRepo.findById(eventId, calendarId, user) ?: return@run failure(EventError.EventNotFound)
            eventRepo.update(user, event, newTitle)
            return@run success(event.copy(title = newTitle))
        }
    }

    fun getEventsOfUser(userId: Int): Either<EventError, List<Event>> {
        return trxManager.run {
            if (userId < 0) return@run failure(EventError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            return@run success(eventRepo.findByUserId(user))
        }
    }

    fun deleteEvent(
        userId: Int,
        eventId: Long,
        calendarId: Long,
    ): Either<EventError, Boolean> {
        return trxManager.run {
            if (eventId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (calendarId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            if (userId < 0) {
                return@run failure(EventError.NegativeIdentifier)
            }
            val user = userRepo.findById(userId) ?: return@run failure(EventError.UserNotFound)
            val event = eventRepo.findById(eventId, calendarId, user) ?: return@run failure(EventError.EventNotFound)
            return@run success(eventRepo.delete(user, event))
        }
    }
}
