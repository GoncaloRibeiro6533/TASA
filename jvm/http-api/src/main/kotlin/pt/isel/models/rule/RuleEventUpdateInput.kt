package pt.isel.models.rule

import kotlinx.datetime.LocalDateTime

data class RuleEventUpdateInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
