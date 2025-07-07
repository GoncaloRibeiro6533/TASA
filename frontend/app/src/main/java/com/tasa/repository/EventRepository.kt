package com.tasa.repository

import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Event
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.EventRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.service.http.models.event.EventInput
import com.tasa.service.interfaces.ServiceWithRetry
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.localMode.EventLocal
import com.tasa.storage.entities.remote.EventRemote
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.NetworkChecker
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class EventRepository(
    private val local: TasaDB,
    private val remote: TasaService,
    private val userInfoRepository: UserInfoRepository,
    private val queryCalendarService: QueryCalendarService,
    private val networkChecker: NetworkChecker,
    userRepo: UserRepository,
) : EventRepositoryInterface, ServiceWithRetry(userRepo) {
    private suspend fun hasEvents(): Boolean {
        if (userInfoRepository.isLocal()) {
            return local.localDao().hasEvents()
        } else {
            return local.remoteDao().hasEvents()
        }
    }

    private suspend fun getToken(): String {
        return userInfoRepository.getToken() ?: throw AuthenticationException(
            "User is not authenticated. Please log in again.",
            null,
        )
    }

    suspend fun getFromApi(): Either<ApiError, List<Event>> {
        val result =
            retryOnFailure {
                remote.eventService.fetchEventAll(getToken())
            }
        return when (result) {
            is Success -> {
                success(
                    result.value.mapNotNull { event ->
                        queryCalendarService.toLocalEvent(
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

    override suspend fun fetchEvents(): Either<ApiError, Flow<List<Event>>> {
        if (userInfoRepository.isLocal()) {
            return success(
                local.localDao().getEventsFlow().map { eventEntities ->
                    eventEntities.map { eventEntity -> eventEntity.toEvent() }
                },
            )
        }
        if (local.remoteDao().hasEvents()) {
            return success(
                local.remoteDao().getEventsFlow().map { eventEntities ->
                    eventEntities.map { eventEntity -> eventEntity.toEvent() }
                },
            )
        } else {
            when (val events = getFromApi()) {
                is Success -> {
                    local.remoteDao().insertEventRemote(
                        *events.value.map { event ->
                            event.toEventRemote()
                        }.toTypedArray(),
                    )
                    return success(
                        local.remoteDao().getEventsFlow().map { eventEntities ->
                            eventEntities.map { eventEntity -> eventEntity.toEvent() }
                        },
                    )
                }
                is Failure -> return failure(events.value)
            }
        }
    }

    override suspend fun getEventById(id: Int): Event? {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getEventById(id.toLong())?.toEvent()
        } else {
            local.remoteDao().getEventById(id)?.toEvent()
        }
    }

    override suspend fun getByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Event? {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getEventByCalendarIdAndEventId(calendarId, eventId)?.toEvent()
        } else {
            local.remoteDao().getEventByCalendarIdAndEventId(calendarId, eventId)?.toEvent()
        }
    }

    override suspend fun updateEvent(event: Event): Either<ApiError, Event> {
        if (userInfoRepository.isLocal()) {
            local.localDao().updateEventLocal(
                id = event.id,
                eventId = event.eventId,
                calendarId = event.calendarId,
                title = event.title,
            )
            return success(event)
        }
        val remoteResult =
            retryOnFailure {
                remote.eventService.updateEventTitle(event, getToken())
            }
        return when (remoteResult) {
            is Success -> {
                local.localDao().updateEventLocal(
                    id = event.id,
                    eventId = event.eventId,
                    calendarId = event.calendarId,
                    title = event.title,
                )
                success(remoteResult.value)
            }
            is Failure -> {
                failure(remoteResult.value)
            }
        }
    }

    override suspend fun deleteEvent(event: Event): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal()) {
            local.localDao().deleteEventLocalById(event.id)
            return success(Unit)
        } else {
            val remoteResult =
                retryOnFailure {
                    remote.eventService.deleteEventById(event.id, getToken())
                }
            return when (remoteResult) {
                is Success -> {
                    local.remoteDao().deleteEventRemoteById(event.id)
                    success(Unit)
                }
                is Failure -> failure(remoteResult.value)
            }
        }
    }

    override suspend fun clear() {
        local.localDao().clearEvents()
    }

    override suspend fun syncEvents(): Either<ApiError, Unit> {
        TODO()
    }

    override suspend fun insertEvent(
        calendarId: Long,
        eventId: Long,
        title: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Either<ApiError, Event> {
        if (userInfoRepository.isLocal()) {
            val id =
                local.localDao().insertEventLocal(
                    EventLocal(
                        eventId = eventId,
                        calendarId = calendarId,
                        title = title,
                    ),
                )
            return success(
                Event(
                    id = id.toInt(),
                    eventId = eventId,
                    calendarId = calendarId,
                    title = title,
                ),
            )
        }
        val remoteResult =
            retryOnFailure {
                remote.eventService.insertEvent(
                    EventInput(
                        title = title,
                        startTime = startTime,
                        endTime = endTime,
                    ),
                    getToken(),
                )
            }
        return when (remoteResult) {
            is Success -> {
                val entity =
                    EventRemote(
                        id = remoteResult.value.id,
                        eventId = eventId,
                        calendarId = calendarId,
                        title = title,
                    )
                local.remoteDao().insertEventRemote(entity)
                success(entity.toEvent())
            }
            is Failure -> failure(remoteResult.value)
        }
    }
}
