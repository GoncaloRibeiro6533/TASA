package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.GeofenceEntity

@Dao
interface GeofenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofence(geofence: GeofenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeofences(geofences: List<GeofenceEntity>): List<Long>

    @Query("SELECT * FROM geofence WHERE id = :id")
    suspend fun getGeofenceById(id: Int): GeofenceEntity?

    @Query("SELECT * FROM geofence WHERE name = :name")
    suspend fun getGeofenceByName(name: String): GeofenceEntity?

    @Query("SELECT * FROM geofence")
    suspend fun getAllGeofences(): List<GeofenceEntity>

    @Query("DELETE FROM geofence WHERE id = :id")
    suspend fun deleteGeofenceById(id: Int): Int

    @Query("DELETE FROM geofence")
    suspend fun clear(): Int

    @Query("DELETE FROM geofence WHERE name = :name")
    suspend fun deleteGeofenceByName(name: String): Int

    @Query("SELECT COUNT(*) FROM geofence")
    suspend fun countGeofences(): Int

    @Query("SELECT * FROM geofence WHERE latitude = :latitude AND longitude = :longitude")
    suspend fun getGeofenceByLocation(
        latitude: Double,
        longitude: Double,
    ): GeofenceEntity?

    @Query("UPDATE geofence SET name = :name, latitude = :latitude, longitude = :longitude, radius = :radius WHERE id = :id")
    suspend fun updateGeofence(
        id: Int,
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Int
}
