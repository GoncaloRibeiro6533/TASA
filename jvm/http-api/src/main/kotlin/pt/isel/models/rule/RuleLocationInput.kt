package pt.isel.models.rule

import kotlinx.datetime.LocalDateTime

data class RuleLocationInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val locationId: Int,
)
