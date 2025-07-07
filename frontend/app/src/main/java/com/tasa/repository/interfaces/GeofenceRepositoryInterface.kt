package com.tasa.repository.interfaces

import com.tasa.domain.Location
import com.tasa.domain.RuleLocationTimeless

data class Geofence(
    val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val ruleId: Int,
    val name: String,
)

interface GeofenceRepositoryInterface {
    suspend fun getGeofenceById(id: Int): Geofence?

    suspend fun getAllGeofences(): List<Geofence>

    suspend fun createGeofence(
        location: Location,
        rule: RuleLocationTimeless,
    ): Int

    suspend fun updateGeofence(
        geofence: Geofence,
        location: Location,
    )

    suspend fun deleteGeofence(geofence: Geofence)

    suspend fun clear()
}
