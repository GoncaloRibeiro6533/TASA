package com.tasa.service.http.models.event

import java.time.LocalDateTime

@Suppress("unused")
data class EventInput(
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
