package com.tasa.storage.entities.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tasa.domain.Event

@Entity(
    tableName = "event_remote",
)
data class EventRemote(
    @PrimaryKey
    val id: Int,
    val eventId: Long,
    val calendarId: Long,
    val title: String,
) {
    fun toEvent(): Event {
        return Event(
            id = id,
            eventId = eventId,
            calendarId = calendarId,
            title = title,
        )
    }
}
