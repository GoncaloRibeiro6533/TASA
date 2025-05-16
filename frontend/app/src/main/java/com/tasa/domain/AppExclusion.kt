package com.tasa.domain

/**
 * Represents an app exclusion to violate the silent mode.
 * @property id the app exclusion's id
 * @property name the app exclusion's name
 * @throws IllegalArgumentException if the name is invalid
 */
class AppExclusion(
    id: Int,
    val name: String,
) : Exclusion(id) {
    init {
        require(name.isNotBlank()) { "name must not be blank" }
        require(name.length <= MAX_NAME_LENGTH) {
            "name must not be longer than $MAX_NAME_LENGTH"
        }
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
    }

    fun copy(
        id: Int = this.id,
        name: String = this.name,
    ): AppExclusion {
        return AppExclusion(id, name)
    }

    override fun toString(): String {
        return "AppExclusion(id=$id, name='$name')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AppExclusion) return false

        if (id != other.id) return false
        if (name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
