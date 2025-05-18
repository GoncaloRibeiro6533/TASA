package com.tasa.storage.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tasa.domain.Action
import com.tasa.domain.Alarm


@Entity(tableName = "alarm")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val triggerTime: Long,
    val action: Action,
){
    fun toAlarm(): Alarm {
        return Alarm(
            id = id,
            triggerTime = triggerTime,
            action = action,
        )
    }
}
