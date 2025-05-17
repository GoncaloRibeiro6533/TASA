package com.tasa.domain

import com.tasa.domain.user.User
import java.time.LocalDateTime

class RuleLocation(
    id: Int? = null,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    creator: User,
    val location: Location,
) : Rule(id, startTime, endTime, creator) {
    override fun toString(): String {
        return "RuleLocation(id=$id, startTime=$startTime, endTime=$endTime, creator=$creator,location=$location)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuleLocation) return false
        if (!super.equals(other)) return false
        if (location != other.location) return false
        return true
    }

    fun copy(
        id: Int? = this.id,
        startTime: LocalDateTime = this.startTime,
        endTime: LocalDateTime = this.endTime,
        creator: User = this.creator,
        location: Location = this.location,
    ): RuleLocation {
        return RuleLocation(id, startTime, endTime, creator, location)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + location.hashCode()
        return result
    }
}
