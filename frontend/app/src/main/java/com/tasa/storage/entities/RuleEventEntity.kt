package com.tasa.storage.entities

import androidx.room.Entity
import androidx.room.ForeignKey

data class EventKey(
    val eventId: Long,
    val calendarId: Long,
)

@Entity(
    tableName = "rule_event",
    primaryKeys = ["endTime", "startTime"],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["eventId", "calendarId"],
            childColumns = ["eventId", "calendarId"],
            // onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RuleEventEntity(
    val id: Int? = null,
    val startTime: Long,
    val endTime: Long,
    val eventId: Long,
    val calendarId: Long,
)
