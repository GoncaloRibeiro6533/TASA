package pt.isel

import java.time.LocalDateTime

/**
 * Represents an event.
 * @property id the event's id.
 * @property startTime the event's start time
 * @property endTime the event's end time
 * @property title the event's title
 */
data class Event(
    val id: Int,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    init {
        require(id >= 0) { "id must be positive" }
        require(title.isNotBlank()) { "title must not be blank" }
        require(title.length <= MAX_TITLE_LENGTH) {
            "title must not be longer than $MAX_TITLE_LENGTH"
        }
        require(startTime < endTime) { "startTime must be before endTime" }
    }

    companion object {
        const val MAX_TITLE_LENGTH = 50
    }
}
