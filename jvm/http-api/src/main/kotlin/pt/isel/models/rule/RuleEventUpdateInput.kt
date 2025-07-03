package pt.isel.models.rule

import java.time.LocalDateTime

data class RuleEventUpdateInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
