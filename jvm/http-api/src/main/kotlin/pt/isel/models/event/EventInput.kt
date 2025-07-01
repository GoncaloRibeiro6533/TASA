package pt.isel.models.event

import kotlinx.datetime.LocalDateTime

data class EventInput(
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
