package com.tasa.domain

import com.tasa.storage.entities.RuleLocationTimelessEntity

class RuleLocationTimeless(
    id: Int? = null,
    val location: Location,
) : RuleBase(id) {
    fun toEntity(geofenceId: Int): RuleLocationTimelessEntity {
        return RuleLocationTimelessEntity(
            id = id,
            locationName = location.name,
            geofenceId = geofenceId,
        )
    }

    fun copy(id: Int?): RuleLocationTimeless {
        return RuleLocationTimeless(
            id = id ?: this.id,
            location = this.location,
        )
    }

    override fun toString(): String {
        return "RuleLocationTimeless(id=$id, location=$location)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuleLocationTimeless) return false
        if (!super.equals(other)) return false
        if (id != other.id) return false
        if (location != other.location) return false
        return true
    }

    override fun hashCode(): Int {
        return 31 * super.hashCode() + location.hashCode()
    }
}
