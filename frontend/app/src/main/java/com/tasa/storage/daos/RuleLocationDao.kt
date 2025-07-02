package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.RuleLocationEntity
import com.tasa.storage.entities.RuleLocationWithLocation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface RuleLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleLocation(ruleLocation: RuleLocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleLocations(ruleLocations: List<RuleLocationEntity>)

    @Query("SELECT * FROM rule_location WHERE id = :id")
    fun getRuleLocationById(id: Int): Flow<RuleLocationEntity?>

    @Query("SELECT * FROM rule_location join location on rule_location.locationName = location.name")
    fun getAllRuleLocations(): Flow<List<RuleLocationWithLocation>>

    @Query("SELECT * FROM rule_location WHERE startTime >= :startTime AND endTime <= :endTime")
    fun getRuleLocationsInRange(
        startTime: Long,
        endTime: Long,
    ): Flow<List<RuleLocationEntity>>

    @Query(
        """
    SELECT rule_location.*, location.* FROM rule_location
    INNER JOIN location ON rule_location.locationName = location.name
    WHERE rule_location.startTime = :startTime AND rule_location.endTime = :endTime
    """,
    )
    suspend fun getRuleLocationByTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): RuleLocationWithLocation?

    @Query("SELECT * FROM rule_location WHERE locationName = :locationName")
    fun getRuleLocationsByLocationName(locationName: String): Flow<List<RuleLocationEntity>>

    @Query(
        """
        SELECT rule_location.*, location.* FROM rule_location
                INNER JOIN location ON rule_location.locationName = location.name 
                WHERE rule_location.locationName = :locationName
    """,
    )
    suspend fun getRuleLocationsByLocationNameResult(locationName: String): List<RuleLocationWithLocation>

    @Query("DELETE FROM rule_location WHERE id = :id")
    suspend fun deleteRuleLocationById(id: Int)

    @Query("DELETE FROM rule_location WHERE locationName = :locationName")
    suspend fun deleteRuleLocationByName(locationName: String)

    @Query("DELETE FROM rule_location")
    suspend fun clear()

    @Query("SELECT COUNT(*) FROM rule_location WHERE startTime >= :startTime AND endTime <= :endTime")
    suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean

    @Query("SELECT COUNT(*)> 0 FROM rule_location")
    suspend fun hasRules(): Boolean

    @Query(
        """
        SELECT rule_location.*, location.* FROM rule_location
                INNER JOIN location ON rule_location.locationName = location.name 
                WHERE rule_location.id = :id
    """,
    )
    fun fetchRuleLocationById(id: Int): RuleLocationWithLocation?
}
