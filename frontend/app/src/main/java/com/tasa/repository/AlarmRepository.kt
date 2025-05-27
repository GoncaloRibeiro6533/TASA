package com.tasa.repository

import com.tasa.domain.Action
import com.tasa.domain.Alarm
import com.tasa.repository.interfaces.AlarmRepositoryInterface
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.AlarmEntity

class AlarmRepository(
    private val local: TasaDB,
) : AlarmRepositoryInterface {
    override suspend fun createAlarm(
        triggerTime: Long,
        action: Action,
    ): Int {
        return local.alarmDao().insertAlarm(
            AlarmEntity(
                id = 0,
                triggerTime = triggerTime,
                action = action,
            ),
        ).toInt()
    }

    override suspend fun getAlarmById(id: Int): Alarm? {
        val alarmEntity = local.alarmDao().getAlarmById(id)
        return alarmEntity?.toAlarm()
    }

    override suspend fun getAllAlarms(): List<Alarm> {
        return local.alarmDao().getAllAlarms().map { it.toAlarm() }
    }

    override suspend fun updateAlarm(
        triggerTime: Long,
        action: Action,
        id: Int,
    ): Int {
        return local.alarmDao().updateAlarm(triggerTime, action, id)
    }

    override suspend fun deleteAlarm(id: Int): Int {
        return local.alarmDao().deleteAlarm(id)
    }

    override suspend fun clear(): Int {
        return local.alarmDao().clear()
    }

    override suspend fun clearOlderAlarms(now: Long) {
        local.alarmDao().deleteExpiredAlarms(now)
    }
}
