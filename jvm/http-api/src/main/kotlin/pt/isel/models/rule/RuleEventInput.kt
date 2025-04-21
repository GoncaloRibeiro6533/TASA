package pt.isel.models.rule

import kotlinx.datetime.Instant

data class RuleEventInput(
    val eventId: Long,
    val calendarId: Long,
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
)
