package pt.isel

import kotlinx.datetime.Instant

/**
 * Represents a rule of silence defined by its id, starTime and endTime.
 * @property id the rule's id
 * @property starTime the rule's start time
 * @property endTime the rule's end time
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
data class Rule(
    val id: Int,
    val starTime: Instant,
    val endTime: Instant,
) {
    init {
        require(id >= 0) { "id must be positive" }
        require(endTime > starTime) { "endTime must be after starTime" }
    }
}
