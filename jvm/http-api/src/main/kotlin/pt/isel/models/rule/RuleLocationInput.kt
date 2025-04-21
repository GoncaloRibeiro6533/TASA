package pt.isel.models.rule

import kotlinx.datetime.Instant

data class RuleLocationInput(
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
)
