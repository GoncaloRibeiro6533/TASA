package com.tasa.storage.entities

import androidx.room.Entity
import com.tasa.domain.Event

@Entity(
    tableName = "event",
    primaryKeys = ["eventId", "calendarId"],
)
data class EventEntity(
    val externalId: Int? = null,
    val eventId: Long,
    val calendarId: Long,
    val title: String,
) {
    fun toEvent(): Event {
        return Event(
            id = eventId,
            calendarId = calendarId,
            title = title,
        )
    }
}
