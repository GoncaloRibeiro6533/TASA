package pt.isel.models.rule

import kotlinx.datetime.Instant
import pt.isel.Event

data class RuleEventOutput(
    val id: Int,
    val startTime: Instant,
    val endTime: Instant,
    val event: Event,
)
