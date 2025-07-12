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

/* *
 *  Represents a repository interface for managing geofences.
 *  Provides methods to create, update, delete, and retrieve geofences.
 */
interface GeofenceRepositoryInterface {
    /**
     * Retrieves a geofence by its name.
     * @param id The id of the geofence to be retrieved.
     * @return The Geofence object if found, or null if not found.
     */
    suspend fun getGeofenceById(id: Int): Geofence?

    /**
     * Retrieves a list of all geofences.
     * @return A list of all Geofence objects.
     */
    suspend fun getAllGeofences(): List<Geofence>

    /**
     * Creates a new geofence with the specified location and rule.
     * @param location The location where the geofence is to be created.
     * @param rule The rule associated with the geofence.
     * @return The ID of the created geofence.
     */
    suspend fun createGeofence(
        location: Location,
        rule: RuleLocationTimeless,
    ): Int

    /**
     * Updates an existing geofence with the specified location.
     * @param geofence The geofence to be updated.
     * @param location The new location for the geofence.
     */
    suspend fun updateGeofence(
        geofence: Geofence,
        location: Location,
    )

    /**
     * Deletes a geofence.
     * @param geofence The geofence to be deleted.
     */
    suspend fun deleteGeofence(geofence: Geofence)

    /**
     * Clears all geofences from the repository.
     * @return Unit if successful.
     */
    suspend fun clear()
}
