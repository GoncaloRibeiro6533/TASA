package com.tasa.domain

import com.tasa.storage.entities.RuleLocationTimelessEntity

data class RuleLocationTimeless(
    override val id: Int? = null,
    val location: Location,
) : Rule(id), TimelessRule {
    fun toEntity(geofenceId: Int): RuleLocationTimelessEntity {
        return RuleLocationTimelessEntity(
            id = id,
            locationName = location.name,
            geofenceId = geofenceId,
        )
    }
}
