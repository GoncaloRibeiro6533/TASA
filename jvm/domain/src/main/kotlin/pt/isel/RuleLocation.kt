package pt.isel

class RuleLocation(
    id: Int,
    creator: User,
    val location: Location,
) : Rule(id, creator) {
    override fun toString(): String {
        return "RuleLocation(id=$id, creator=$creator, location=$location)"
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
        creator: User = this.creator,
        location: Location = this.location,
    ): RuleLocation {
        return RuleLocation(id, creator, location)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + location.hashCode()
        return result
    }
}
