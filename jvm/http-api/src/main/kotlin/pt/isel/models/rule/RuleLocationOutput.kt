package pt.isel.models.rule

import kotlinx.datetime.LocalDateTime
import pt.isel.Location

data class RuleLocationOutput(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: Location,
)
