package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.tasa.storage.entities.RuleEventEntity
import com.tasa.storage.entities.RuleEventWithEvent
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

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

    @Query(
        """
    SELECT rule_event.*, event.* FROM rule_event
    INNER JOIN event ON rule_event.eventId = event.eventId AND rule_event.calendarId = event.calendarId
    WHERE rule_event.startTime = :startTime AND rule_event.endTime = :endTime
    """,
    )
    suspend fun getRuleEventByStartAndEndTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): RuleEventWithEvent?

    @Query(
        """
    SELECT rule_event.*, event.* FROM rule_event
    JOIN event 
    ON rule_event.eventId = event.eventId AND rule_event.calendarId = event.calendarId
    """,
    )
    fun getAllRuleEvents(): Flow<List<RuleEventWithEvent>>

    @Query("UPDATE rule_event SET startTime = :startTime, endTime = :endTime WHERE id = :id")
    suspend fun updateRuleEvent(
        id: Int,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    )

    @Query("UPDATE rule_event SET startTime = :startTime, endTime = :endTime WHERE startTime = :oldStartTime AND endTime = :oldEndTime")
    suspend fun updateRuleEventByStartAndEndTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        oldStartTime: LocalDateTime,
        oldEndTime: LocalDateTime,
    )

    @Query("DELETE FROM rule_event WHERE startTime = :startTime AND endTime = :endTime")
    suspend fun deleteRuleEventByStartAndEndTime(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    )

    @Query("DELETE FROM rule_event WHERE eventId = :eventId AND calendarId = :calendarId")
    suspend fun deleteRuleEventByEventIdAndCalendarId(
        eventId: Long,
        calendarId: Long,
    )

    @Query("DELETE FROM rule_event WHERE id = :id")
    suspend fun deleteRuleEventById(id: Int)

    @Query("DELETE FROM rule_event")
    suspend fun clear()

    @Query("SELECT COUNT(*) > 0 FROM rule_event WHERE startTime <= :endTime AND endTime >= :startTime")
    suspend fun isCollision(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Boolean

    @Query("DELETE FROM rule_event WHERE eventId = :eventId AND calendarId = :calendarId AND startTime = :startTime AND endTime = :endTime")
    suspend fun deleteRuleEventByEventIdAndCalendarIdAndStarTimeAndEndTime(
        eventId: Long,
        calendarId: Long,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    )
}
