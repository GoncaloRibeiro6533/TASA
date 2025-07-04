package com.tasa.repository

import android.content.Context
import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Event
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.EventRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.EventEntity
import com.tasa.ui.screens.calendar.utils.toLocalEvent
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepository(
    private val local: TasaDB,
    private val remote: TasaService,
    private val userInfoRepository: UserInfoRepository,
) : EventRepositoryInterface {
    private suspend fun hasEvents(): Boolean {
        return local.eventDao().hasEvents()
    }

    private suspend fun getToken(): String {
        return userInfoRepository.getToken() ?: throw AuthenticationException(
            "User is not authenticated. Please log in again.",
            null,
        )
    }

    private suspend fun getFromApi(context: Context): Either<ApiError, List<Event>> {
        val result = remote.eventService.fetchEventAll(getToken())
        return when (result) {
            is Success -> {
                success(
                    result.value.mapNotNull { event ->
                        context.toLocalEvent(
                            event.id,
                            event.title,
                            event.startTime,
                            event.endTime,
                        )
                    },
                )
            }
            is Failure -> failure(result.value)
        }
    }

    override suspend fun fetchEvents(context: Context): Either<ApiError, Flow<List<EventEntity>>> {
        return if (hasEvents() || userInfoRepository.isLocal()) {
            success(local.eventDao().getAllEvents())
        } else {
            when (val events = getFromApi(context)) {
                is Success -> {
                    local.eventDao().insertEvents(
                        *events.value.map { event ->
                            event.toEventEntity()
                        }.toTypedArray(),
                    )
                    success(local.eventDao().getAllEvents())
                }
                is Failure -> failure(events.value)
            }
        }
    }

    override suspend fun fetchEventsByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Flow<Event?> {
        return local.eventDao().getEventById(eventId, calendarId)
            .map { event ->
                event.let {
                    Event(
                        id = it.externalId,
                        eventId = it.eventId,
                        calendarId = it.calendarId,
                        title = it.title,
                    )
                }
            }
    }

    override suspend fun insertEvent(event: Event) {
        local.eventDao().insertEvents(event.toEventEntity())
    }

    override suspend fun insertEvents(events: List<Event>) {
        local.eventDao().insertEvents(*events.map { it.toEventEntity() }.toTypedArray())
    }

    override suspend fun updateEvent(event: Event): Either<ApiError, Event> {
        val remoteResult = remote.eventService.updateEventTitle(event, getToken())
        return when (remoteResult) {
            is Success -> {
                local.eventDao().updateEvent(
                    eventId = event.eventId,
                    calendarId = event.calendarId,
                    title = remoteResult.value.title,
                )
                success(remoteResult.value)
            }
            is Failure -> {
                failure(remoteResult.value)
            }
        }
    }

    override suspend fun deleteEvent(event: Event): Either<ApiError, Unit> {
        if (event.id == null) {
            return failure(ApiError("Event ID cannot be zero."))
        }
        val remoteResult = remote.eventService.deleteEventById(event.id, getToken())
        return when (remoteResult) {
            is Success -> {
                local.eventDao().deleteEvent(event.eventId, event.calendarId)
                success(Unit)
            }

            is Failure -> failure(remoteResult.value)
        }
    }

    override suspend fun clear() {
        local.eventDao().clear()
    }
}
