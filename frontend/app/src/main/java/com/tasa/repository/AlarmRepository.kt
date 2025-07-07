package com.tasa.repository

import com.tasa.domain.Action
import com.tasa.domain.Alarm
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.AlarmRepositoryInterface
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.localMode.AlarmLocal
import com.tasa.storage.entities.remote.AlarmRemote

class AlarmRepository(
    private val local: TasaDB,
    private val userInfoRepository: UserInfoRepository,
) : AlarmRepositoryInterface {
    override suspend fun createAlarm(
        triggerTime: Long,
        action: Action,
        ruleId: Int,
    ): Int {
        if (userInfoRepository.isLocal()) {
            return local.localDao().insertAlarmLocal(
                AlarmLocal(
                    triggerTime = triggerTime,
                    action = action,
                    ruleId = ruleId,
                ),
            ).toInt()
        } else {
            return local.remoteDao().insertAlarmRemote(
                AlarmRemote(
                    triggerTime = triggerTime,
                    action = action,
                    ruleId = ruleId,
                ),
            ).toInt()
        }
    }

    override suspend fun getAlarmByTriggerTime(currentTime: Long): Alarm? {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getAlarmByTriggerTime(currentTime)?.toAlarm()
        } else {
            local.remoteDao().getAlarmByTriggerTime(currentTime)?.toAlarm()
        }
    }

    override suspend fun getAlarmById(id: Int): Alarm? {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getAlarmById(id)?.toAlarm()
        } else {
            local.remoteDao().getAlarmById(id)?.toAlarm()
        }
    }

    override suspend fun getAllAlarms(): List<Alarm> {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getAllAlarms().map { it.toAlarm() }
        } else {
            local.remoteDao().getAllAlarms().map { it.toAlarm() }
        }
    }

    override suspend fun updateAlarm(
        triggerTime: Long,
        action: Action,
        id: Int,
    ) {
        return if (userInfoRepository.isLocal()) {
            local.localDao().updateAlarmLocal(
                id = id,
                time = triggerTime,
            )
        } else {
            local.remoteDao().updateAlarmRemote(
                id = id,
                time = triggerTime,
            )
        }
    }

    override suspend fun deleteAlarm(id: Int) {
        return if (userInfoRepository.isLocal()) {
            local.localDao().deleteAlarmLocalById(id)
        } else {
            local.remoteDao().deleteAlarmRemoteById(id)
        }
    }

    override suspend fun clear() {
        if (userInfoRepository.isLocal()) {
            local.localDao().clearAlarms()
        } else {
            local.remoteDao().clearAlarms()
        }
    }

    override suspend fun clearOlderAlarms(now: Long) {
        if (userInfoRepository.isLocal()) {
            local.localDao().clearOldAlarms(now)
        } else {
            local.remoteDao().clearOldAlarms(now)
        }
    }
}
