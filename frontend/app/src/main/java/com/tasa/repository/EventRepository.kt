package com.tasa.repository

import com.tasa.domain.Event
import com.tasa.domain.TasaException
import com.tasa.repository.interfaces.EventRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.EventEntity
import com.tasa.utils.Failure
import com.tasa.utils.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepository(
    private val local: TasaDB,
    private val remote: TasaService,
) : EventRepositoryInterface {
    private suspend fun hasEvents(): Boolean {
        return local.eventDao().hasEvents()
    }

    private suspend fun getFromApi() = remote.eventService.fetchEvents()

    override suspend fun fetchEvents(): Flow<List<EventEntity>> {
        return if (hasEvents()) {
            local.eventDao().getAllEvents()
        } else {
            when (val events = getFromApi()) {
                is Success -> {
                    local.eventDao().insertEvents(
                        *events.value.map { event ->
                            EventEntity(
                                event.id,
                                event.calendarId,
                                event.title,
                            )
                        }.toTypedArray(),
                    )
                    local.eventDao().getAllEvents()
                }
                is Failure -> throw TasaException(events.value.message, null)
            }
        }
    }

    override suspend fun fetchEventsByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Flow<Event?> {
        return local.eventDao().getEventById(eventId, calendarId)
            .map { event ->
                event?.let {
                    Event(
                        it.eventId,
                        it.calendarId,
                        it.title,
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

    override suspend fun updateEvent(event: Event) {
        local.eventDao().updateEvent(event.toEventEntity())
    }

    override suspend fun deleteEvent(event: Event) {
        local.eventDao().deleteEvent(event.id, event.calendarId)
    }

    override suspend fun clear() {
        local.eventDao().clear()
    }
}
