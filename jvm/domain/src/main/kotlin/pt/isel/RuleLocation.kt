package pt.isel

import kotlinx.datetime.Instant

class RuleLocation(
    id: Int,
    startTime: Instant,
    endTime: Instant,
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
        id: Int = this.id,
        startTime: Instant = this.startTime,
        endTime: Instant = this.endTime,
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
