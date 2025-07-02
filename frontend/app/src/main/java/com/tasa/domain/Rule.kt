package com.tasa.domain

import java.time.LocalDateTime

/**
 * Represents a rule of silence defined by its id, startTime and endTime.
 * @property id the rule's id
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
sealed class Rule(open val id: Int?)

interface TimedRule {
    val startTime: LocalDateTime
    val endTime: LocalDateTime
}

interface TimelessRule
