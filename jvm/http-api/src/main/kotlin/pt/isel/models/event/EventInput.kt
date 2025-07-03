package pt.isel.models.event

import java.time.LocalDateTime

data class EventInput(
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
