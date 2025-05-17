package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.tasa.storage.entities.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(vararg events: EventEntity)

    @Query("SELECT * FROM event")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM event WHERE calendarId = :calendarId AND eventId = :eventId")
    suspend fun getEventById(
        eventId: String,
        calendarId: String,
    ): EventEntity?

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("DELETE FROM event")
    suspend fun clear()

    @Query("DELETE FROM event WHERE eventId = :eventId AND calendarId = :calendarId")
    suspend fun deleteEvent(
        eventId: String,
        calendarId: String,
    )
}
