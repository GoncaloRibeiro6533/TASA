package com.tasa.service.http.models.event

import com.tasa.domain.Event
import java.time.LocalDateTime

data class EventOutput(
    val id: Int,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    fun toEvent(
        eventId: Long,
        calendarId: Long,
    ): Event {
        return Event(
            externalId = id,
            calendarId = calendarId,
            id = eventId,
            title = title,
        )
    }
}
