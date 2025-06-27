package com.tasa.domain

import com.tasa.storage.entities.RuleLocationTimelessEntity

data class RuleLocationTimeless(
    val id: Int? = null,
    val location: Location,
) {
    fun toEntity(geofenceId: Int): RuleLocationTimelessEntity {
        return RuleLocationTimelessEntity(
            id = id,
            locationName = location.name,
            geofenceId = geofenceId,
        )
    }
}
