package com.tasa.domain

/**
 * Represents an exception defined by its id.
 * @property id the exception's id
 * @throws IllegalArgumentException if the id is not positive
 */
sealed class Exclusion(
    val id: Int,
) {
    init {
        require(id >= 0) { "id must be positive" }
    }
}
