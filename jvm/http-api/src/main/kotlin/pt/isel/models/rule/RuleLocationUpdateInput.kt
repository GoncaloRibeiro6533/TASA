package pt.isel.models.rule

import kotlinx.datetime.LocalDateTime

data class RuleLocationUpdateInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
