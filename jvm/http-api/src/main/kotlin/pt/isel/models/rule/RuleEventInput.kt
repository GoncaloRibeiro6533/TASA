package pt.isel.models.rule

import kotlinx.datetime.LocalDateTime

data class RuleEventInput(
    val eventId: Long,
    val calendarId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
