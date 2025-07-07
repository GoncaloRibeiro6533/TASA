package com.tasa.storage.entities.localMode

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tasa.domain.Event

@Entity(
    tableName = "event_local",
)
data class EventLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
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
