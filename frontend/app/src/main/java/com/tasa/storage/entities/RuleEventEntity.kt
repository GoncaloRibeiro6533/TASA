package com.tasa.storage.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Relation
import com.tasa.domain.RuleEvent
import java.time.LocalDateTime

@Entity(
    tableName = "rule_event",
    primaryKeys = ["endTime", "startTime"],
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["eventId", "calendarId"],
            childColumns = ["eventId", "calendarId"],
            // onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class RuleEventEntity(
    val id: Int? = null,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val eventId: Long,
    val calendarId: Long,
)

data class RuleEventWithEvent(
    @Embedded val ruleEvent: RuleEventEntity,
    @Relation(
        parentColumn = "eventId",
        entityColumn = "eventId",
        entity = EventEntity::class,
    )
    val event: EventEntity,
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
