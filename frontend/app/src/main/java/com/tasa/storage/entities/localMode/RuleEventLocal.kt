package com.tasa.storage.entities.localMode

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.tasa.domain.RuleEvent
import java.time.LocalDateTime

@Entity(
    tableName = "rule_event_local",
    foreignKeys = [
        ForeignKey(
            entity = EventLocal::class,
            parentColumns = ["id"],
            childColumns = ["externalId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class RuleEventLocal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val externalId: Int,
)

data class RuleEventWithEventLocal(
    @Embedded val ruleEvent: RuleEventLocal,
    @Relation(
        parentColumn = "externalId",
        entityColumn = "id",
        entity = EventLocal::class,
    )
    val event: EventLocal,
) {
    fun toRuleEvent(): RuleEvent {
        return RuleEvent(
            id = ruleEvent.id,
            startTime = ruleEvent.startTime,
            endTime = ruleEvent.endTime,
            event = event.toEvent(),
        )
    }
}
