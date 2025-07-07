package com.tasa.domain

import com.tasa.storage.entities.localMode.EventLocal
import com.tasa.storage.entities.remote.EventRemote

/**
 * Represents an event.
 * @property id the event's id.
 * @property eventId the event's unique identifier.
 * @property calendarId the calendar's id to which the event belongs.
 * @property title the event's title
 */
data class Event(
    val id: Int,
    val eventId: Long,
    val calendarId: Long,
    val title: String,
) {
    init {
        require(title.isNotBlank()) { "title must not be blank" }
        require(title.length <= MAX_TITLE_LENGTH) {
            "title must not be longer than $MAX_TITLE_LENGTH"
        }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 50
    }

    fun toEventRemote(): EventRemote {
        return EventRemote(
            id = id,
            eventId = eventId,
            calendarId = calendarId,
            title = title,
        )
    }

    fun toEventLocal(): EventLocal {
        return EventLocal(
            id = id,
            eventId = eventId,
            calendarId = calendarId,
            title = title,
        )
    }
}
