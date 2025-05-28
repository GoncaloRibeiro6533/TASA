package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tasa.storage.entities.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(vararg events: EventEntity)

    @Query("SELECT * FROM event")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event WHERE calendarId = :calendarId AND eventId = :eventId")
    fun getEventById(
        eventId: Long,
        calendarId: Long,
    ): Flow<EventEntity>

    @Query("UPDATE event SET title = :title WHERE eventId = :eventId AND calendarId = :calendarId")
    suspend fun updateEvent(
        eventId: Long,
        calendarId: Long,
        title: String,
    )

    @Query("DELETE FROM event")
    suspend fun clear()

    @Query("DELETE FROM event WHERE eventId = :eventId AND calendarId = :calendarId")
    suspend fun deleteEvent(
        eventId: Long,
        calendarId: Long,
    )

    @Query("SELECT COUNT(*) > 0 FROM event")
    suspend fun hasEvents(): Boolean
}
