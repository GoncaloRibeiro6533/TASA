package com.tasa.service.http.models.event

import com.tasa.domain.Event
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class EventOutput(
    val id: Int,
    val title: String,
    @Contextual
    val startTime: LocalDateTime,
    @Contextual
    val endTime: LocalDateTime,
) {
    fun toEvent(
        eventId: Long,
        calendarId: Long,
    ): Event {
        return Event(
            id = id,
            calendarId = calendarId,
            eventId = eventId,
            title = title,
        )
    }
}
