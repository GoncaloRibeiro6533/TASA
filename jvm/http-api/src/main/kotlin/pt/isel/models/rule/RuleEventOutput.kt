package pt.isel.models.rule

import kotlinx.datetime.LocalDateTime
import pt.isel.Event

data class RuleEventOutput(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val event: Event,
)
