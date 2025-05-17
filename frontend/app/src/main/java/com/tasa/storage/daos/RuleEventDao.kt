package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.tasa.storage.entities.RuleEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RuleEventDao {
    @Insert(onConflict = REPLACE)
    suspend fun insertRuleEvent(ruleEvent: RuleEventEntity)

    @Insert(onConflict = REPLACE)
    suspend fun insertRuleEvents(ruleEvents: List<RuleEventEntity>)

    @Query("SELECT * FROM rule_event WHERE id = :id")
    fun getRuleEventById(id: Int): Flow<RuleEventEntity?>

    @Query("SELECT * FROM rule_event WHERE eventId = :eventId AND calendarId = :calendarId")
    fun getRuleEventByEventIdAndCalendarId(
        eventId: Long,
        calendarId: Long,
    ): Flow<RuleEventEntity?>

    @Query("SELECT * FROM rule_event WHERE startTime= :startTime AND endTime= :endTime")
    fun getRuleEventByStartAndEndTime(
        startTime: String,
        endTime: String,
    ): Flow<RuleEventEntity?>

    @Query("SELECT * FROM rule_event")
    fun getAllRuleEvents(): Flow<List<RuleEventEntity>>

    @Query("UPDATE rule_event SET startTime = :startTime, endTime = :endTime WHERE id = :id")
    suspend fun updateRuleEvent(
        id: Int,
        startTime: String,
        endTime: String,
    )

    @Query("UPDATE rule_event SET startTime = :startTime, endTime = :endTime WHERE startTime = :oldStartTime AND endTime = :oldEndTime")
    suspend fun updateRuleEventByStartAndEndTime(
        startTime: String,
        endTime: String,
        oldStartTime: String,
        oldEndTime: String,
    )

    @Query("DELETE FROM rule_event WHERE startTime = :startTime AND endTime = :endTime")
    suspend fun deleteRuleEventByStartAndEndTime(
        startTime: String,
        endTime: String,
    )

    @Query("DELETE FROM rule_event WHERE id = :id")
    suspend fun deleteRuleEventById(id: Int)

    @Query("DELETE FROM rule_event")
    suspend fun clear()
}
