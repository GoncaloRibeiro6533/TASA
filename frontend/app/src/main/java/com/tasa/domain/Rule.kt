package com.tasa.domain

import com.tasa.domain.user.User
import java.time.LocalDateTime

/**
 * Represents a rule of silence defined by its id, startTime and endTime.
 * @property id the rule's id
 * @property startTime the rule's start time
 * @property endTime the rule's end time
 * @property creator the user that created the rule
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
sealed class Rule(
    val id: Int? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val creator: User,
) {
    init {
        require(endTime > startTime) { "endTime must be after starTime" }
    }
}
