package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Query("SELECT * FROM location WHERE id = :id")
    fun getLocationById(id: Int): Flow<LocationEntity?>

    @Query("SELECT * FROM location WHERE name = :name")
    fun getLocationByName(name: String): Flow<LocationEntity?>

    @Query("SELECT * FROM location")
    fun getAllLocations(): Flow<List<LocationEntity>>

    @Query("UPDATE location SET name = :name, latitude = :latitude, longitude = :longitude, radius = :radius WHERE id = :id")
    suspend fun updateLocation(
        id: Int,
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
    )

    @Query("DELETE FROM location WHERE id = :id")
    suspend fun deleteLocationById(id: Int)

    @Query("DELETE FROM location WHERE name = :name")
    suspend fun deleteLocationByName(name: String)

    @Query("DELETE FROM location")
    suspend fun clear()
}
