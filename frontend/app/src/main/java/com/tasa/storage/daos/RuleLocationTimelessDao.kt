package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.RuleLocationTimelessEntity
import com.tasa.storage.entities.RuleLocationTimelessWithLocation
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleLocationTimelessDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleLocationTimeless(ruleLocationTimeless: RuleLocationTimelessEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleLocations(ruleLocations: List<RuleLocationTimelessEntity>)

    @Query("SELECT * FROM rule_location_timeless WHERE id = :id")
    fun getRuleLocationById(id: Int): Flow<RuleLocationTimelessEntity?>

    @Query("SELECT * FROM rule_location_timeless join location on rule_location_timeless.locationName = location.name")
    fun getAllRuleLocations(): Flow<List<RuleLocationTimelessWithLocation>>

    @Query("SELECT * FROM rule_location_timeless WHERE locationName = :locationName")
    fun getRuleLocationsByLocationName(locationName: String): Flow<List<RuleLocationTimelessEntity>>

    @Query(
        """
        SELECT rule_location_timeless.*, location.* FROM rule_location_timeless
                INNER JOIN location ON rule_location_timeless.locationName = location.name 
                WHERE rule_location_timeless.locationName = :locationName
    """,
    )
    suspend fun getRuleLocationsByLocationNameResult(locationName: String): List<RuleLocationTimelessWithLocation>

    @Query("DELETE FROM rule_location_timeless WHERE id = :id")
    suspend fun deleteRuleLocationById(id: Int)

    @Query("DELETE FROM rule_location_timeless WHERE locationName = :locationName")
    suspend fun deleteRuleLocationByName(locationName: String)

    @Query("DELETE FROM rule_location_timeless")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM rule_location_timeless WHERE locationName= :locationName")
    suspend fun exists(locationName: String): Boolean

    @Query(
        "SELECT rule_location_timeless.*, location.* FROM rule_location_timeless " +
            "INNER JOIN location ON rule_location_timeless.locationName = location.name " +
            "WHERE rule_location_timeless.geofenceId = :id",
    )
    suspend fun getRuleLocationByGeofenceId(id: Int): List<RuleLocationTimelessWithLocation>
}
