package com.tasa.storage.entities.localMode

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.tasa.domain.Action
import com.tasa.domain.Alarm

@Entity(
    tableName = "alarm_local",
    foreignKeys = [
        ForeignKey(
            entity = RuleEventLocal::class,
            parentColumns = ["id"],
            childColumns = ["ruleId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class AlarmLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val triggerTime: Long,
    val action: Action,
    val ruleId: Int,
) {
    fun toAlarm(): Alarm {
        return Alarm(
            id = id,
            triggerTime = triggerTime,
            action = action,
            ruleId = ruleId,
        )
    }
}
