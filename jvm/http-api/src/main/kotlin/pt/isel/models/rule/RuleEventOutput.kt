package pt.isel.models.rule

import pt.isel.Event
import java.time.LocalDateTime

data class RuleEventOutput(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val event: Event,
)
