package pt.isel.models.rule

import kotlinx.datetime.Instant

data class RuleEventUpdateInput(
    val eventId: Long,
    val calendarId: Long,
    val startTime: Instant,
    val endTime: Instant,
)
