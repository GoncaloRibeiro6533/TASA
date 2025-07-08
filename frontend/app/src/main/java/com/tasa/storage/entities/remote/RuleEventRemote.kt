package com.tasa.storage.entities.remote

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.tasa.domain.RuleEvent
import java.time.LocalDateTime

@Entity(
    tableName = "rule_event_remote",
    foreignKeys = [
        ForeignKey(
            entity = EventRemote::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        ),
    ],
)
data class RuleEventRemote(
    @PrimaryKey
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val eventId: Int,
)

data class RuleEventWithEventRemote(
    @Embedded val ruleEvent: RuleEventRemote,
    @Relation(
        parentColumn = "eventId",
        entityColumn = "id",
        entity = EventRemote::class,
    )
    val event: EventRemote,
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
