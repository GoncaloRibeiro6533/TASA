package com.tasa.storage.entities

import androidx.room.Entity

@Entity(
    tableName = "event",
    primaryKeys = ["eventId", "calendarId"],
)
data class EventEntity(
    val eventId: Long,
    val calendarId: Long,
    val title: String,
)
