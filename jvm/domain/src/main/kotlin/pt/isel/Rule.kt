package pt.isel

import kotlinx.datetime.Instant

/**
 * Represents a rule of silence defined by its id, startTime and endTime.
 * @property id the rule's id
 * @property startTime the rule's start time
 * @property endTime the rule's end time
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
sealed class Rule(
    val id: Int,
    val startTime: Instant,
    val endTime: Instant,
    val creator: User,
) {
    init {
        require(id >= 0) { "id must be positive" }
        require(endTime > startTime) { "endTime must be after starTime" }
    }
}
