package com.tasa.service.http.models.location

import com.tasa.domain.Location
import com.tasa.storage.entities.remote.LocationRemote
import kotlinx.serialization.Serializable

@Serializable
data class LocationOutput(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
) {
    fun toRemoteLocation(): LocationRemote {
        return LocationRemote(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
        )
    }

    fun toLocation(): Location {
        return Location(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = radius,
        )
    }
}
