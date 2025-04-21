package pt.isel.models.rule

import kotlinx.datetime.Instant

data class RuleLocationUpdateInput(
    val startTime: Instant,
    val endTime: Instant,
)
