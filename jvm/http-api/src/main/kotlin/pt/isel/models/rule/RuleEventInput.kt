package pt.isel.models.rule

import kotlinx.datetime.LocalDateTime

data class RuleEventInput(
    val eventId: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
