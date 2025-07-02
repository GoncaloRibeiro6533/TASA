package com.tasa.domain

import com.tasa.service.http.models.rule.RuleEventInput
import com.tasa.service.http.models.rule.RuleEventUpdateInput
import com.tasa.storage.entities.RuleEventEntity
import com.tasa.ui.screens.rule.EditRuleActivity.RuleParcelableEvent
import java.time.LocalDateTime

class RuleEvent(
    id: Int? = null,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
    val event: Event,
) : Rule(id, startTime, endTime) {
    override fun toString(): String {
        return "RuleEvent(id=$id, startTime=$startTime, endTime=$endTime, event=$event)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuleEvent) return false
        if (!super.equals(other)) return false
        if (event != other.event) return false
        return true
    }

    fun copy(
        id: Int? = this.id,
        startTime: LocalDateTime = this.startTime,
        endTime: LocalDateTime = this.endTime,
        event: Event = this.event,
    ): RuleEvent {
        return RuleEvent(id, startTime, endTime, event)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + event.hashCode()
        return result
    }

    fun toRuleEventEntity(): RuleEventEntity {
        return RuleEventEntity(
            id = id,
            startTime = startTime,
            endTime = endTime,
            eventId = event.id,
            calendarId = event.calendarId,
        )
    }

    override fun copy(
        id: Int?,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): RuleEvent {
        return RuleEvent(
            id = id,
            startTime = startTime,
            endTime = endTime,
            event = this.event,
        )
    }

    fun toRuleEventParcelable(): RuleParcelableEvent {
        return RuleParcelableEvent(
            id = id,
            startTime = startTime,
            endTime = endTime,
            eventTitle = event.title,
            eventId = event.id,
            calendarId = event.calendarId,
        )
    }

    fun toRuleEventInput(eventId: Int): RuleEventInput {
        return RuleEventInput(
            startTime = startTime,
            endTime = endTime,
            eventId = eventId,
        )
    }

    fun toRuleEventUpdateInput(): RuleEventUpdateInput {
        return RuleEventUpdateInput(
            startTime = startTime,
            endTime = endTime,
        )
    }
}
