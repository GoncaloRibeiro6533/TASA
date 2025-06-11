package com.tasa.repository.interfaces

import com.tasa.domain.Location
import com.tasa.storage.entities.GeofenceEntity

interface GeofenceRepositoryInterface {
    suspend fun getGeofenceById(id: Int): GeofenceEntity?

    suspend fun getAllGeofences(): List<GeofenceEntity>

    suspend fun createGeofence(location: Location): Long

    suspend fun updateGeofence(
        geofenceEntity: GeofenceEntity,
        location: Location,
    )

    suspend fun deleteGeofence(geofenceEntity: GeofenceEntity)

    suspend fun clear()
}
