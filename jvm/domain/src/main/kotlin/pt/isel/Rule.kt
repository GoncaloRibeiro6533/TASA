package pt.isel

/**
 * Represents a rule of silence defined by its id, startTime and endTime.
 * @property id the rule's id
 * @property creator the user that created the rule
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
sealed class Rule(
    val id: Int,
    val creator: User,
) {
    init {
        require(id >= 0) { "id must be positive" }
    }
}
