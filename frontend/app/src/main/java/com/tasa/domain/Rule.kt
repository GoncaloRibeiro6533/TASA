package com.tasa.domain

import java.time.LocalDateTime

/**
 * Represents a rule of silence defined by its id, startTime and endTime.
 * @property id the rule's id
 * @property startTime the rule's start time
 * @property endTime the rule's end time
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
sealed class Rule(
    val id: Int? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    init {
        require(endTime > startTime) { "endTime must be after starTime" }
    }

    abstract fun copy(
        id: Int? = this.id,
        startTime: LocalDateTime = this.startTime,
        endTime: LocalDateTime = this.endTime,
    ): Rule
}
