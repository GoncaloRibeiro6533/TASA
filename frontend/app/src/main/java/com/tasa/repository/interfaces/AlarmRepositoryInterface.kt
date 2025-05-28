package com.tasa.repository.interfaces

import com.tasa.domain.Action
import com.tasa.domain.Alarm

interface AlarmRepositoryInterface {
    suspend fun createAlarm(
        triggerTime: Long,
        action: Action,
    ): Int

    suspend fun getAlarmByTriggerTime(currentTime: Long): Alarm?

    suspend fun getAlarmById(id: Int): Alarm?

    suspend fun getAllAlarms(): List<Alarm>

    suspend fun updateAlarm(
        triggerTime: Long,
        action: Action,
        id: Int,
    ): Int

    suspend fun deleteAlarm(id: Int): Int

    suspend fun clear(): Int

    suspend fun clearOlderAlarms(now: Long)
}
