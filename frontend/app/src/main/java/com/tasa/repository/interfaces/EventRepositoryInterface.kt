package com.tasa.repository.interfaces

import com.tasa.domain.Event
import com.tasa.storage.entities.EventEntity
import kotlinx.coroutines.flow.Flow

interface EventRepositoryInterface {
    suspend fun fetchEvents(): Flow<List<EventEntity>>

    suspend fun fetchEventsByCalendarIdAndEventId(
        calendarId: Long,
        eventId: Long,
    ): Flow<Event?>

    suspend fun insertEvent(event: Event)

    suspend fun insertEvents(events: List<Event>)

    suspend fun updateEvent(event: Event)

    suspend fun deleteEvent(event: Event)

    suspend fun clear()
}
