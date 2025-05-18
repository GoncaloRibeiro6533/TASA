package com.tasa.storage.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import com.tasa.domain.Action
import com.tasa.storage.entities.AlarmEntity

@Dao
interface AlarmDao {

    @Insert(onConflict = REPLACE)
    suspend fun insertAlarm(triggerTime: Long, action: Action): Int

    @Query("SELECT * FROM alarm WHERE id = :id")
    suspend fun getAlarmById(id: Int): AlarmEntity?

    @Query("SELECT * FROM alarm")
    suspend fun getAllAlarms(): List<AlarmEntity>

    @Query("Update alarm SET triggerTime = :triggerTime, `action` = :action WHERE id = :id")
    suspend fun updateAlarm(triggerTime: Long, action: Action, id: Int): Int

    @Query("DELETE FROM alarm WHERE id = :id")
    suspend fun deleteAlarm(id: Int): Int

    @Query("DELETE FROM alarm")
    suspend fun clear(): Int


}