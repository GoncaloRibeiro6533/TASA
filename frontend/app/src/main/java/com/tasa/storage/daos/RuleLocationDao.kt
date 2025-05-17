package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.RuleLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleLocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleLocation(ruleLocation: RuleLocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRuleLocations(ruleLocations: List<RuleLocationEntity>)

    @Query("SELECT * FROM rule_location WHERE id = :id")
    fun getRuleLocationById(id: Int): Flow<RuleLocationEntity?>

    @Query("SELECT * FROM rule_location")
    fun getAllRuleLocations(): Flow<List<RuleLocationEntity>>

    @Query("SELECT * FROM rule_location WHERE startTime >= :startTime AND endTime <= :endTime")
    fun getRuleLocationsInRange(
        startTime: Long,
        endTime: Long,
    ): Flow<List<RuleLocationEntity>>

    @Query("SELECT * FROM rule_location WHERE startTime= :startTime AND endTime <= :endTime")
    fun getRuleLocationsByTime(
        startTime: Long,
        endTime: Long,
    ): Flow<List<RuleLocationEntity>>

    @Query("SELECT * FROM rule_location WHERE locationId = :locationId")
    fun getRuleLocationsByLocationId(locationId: Int): Flow<List<RuleLocationEntity>>

    @Query("DELETE FROM rule_location WHERE id = :id")
    suspend fun deleteRuleLocationById(id: Int)

    @Query("DELETE FROM rule_location")
    suspend fun clear()
}
