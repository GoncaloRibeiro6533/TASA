package com.tasa.service.http.models.event

@Suppress("unused")
data class EventInput(
    val eventId: Long,
    val calendarId: Long,
    val title: String,
)
