package pt.isel.models.rule

import kotlinx.datetime.Instant
import pt.isel.Location

data class RuleLocationOutput(
    val id: Int,
    val startTime: Instant,
    val endTime: Instant,
    val location: Location,
)
