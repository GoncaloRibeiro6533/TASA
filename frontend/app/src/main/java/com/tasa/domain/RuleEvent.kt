package com.tasa.domain

import com.tasa.service.http.models.rule.RuleEventInput
import com.tasa.service.http.models.rule.RuleEventUpdateInput
import com.tasa.storage.entities.RuleEventEntity
import com.tasa.ui.screens.rule.EditRuleActivity.RuleParcelableEvent
import java.time.LocalDateTime

data class RuleEvent(
    override val id: Int? = null,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
    val event: Event,
) : Rule(id), TimedRule {
    fun toRuleEventEntity(): RuleEventEntity {
        return RuleEventEntity(
            id = id,
            startTime = startTime,
            endTime = endTime,
            eventId = event.eventId,
            calendarId = event.calendarId,
        )
    }

    fun toRuleEventParcelable(): RuleParcelableEvent {
        return RuleParcelableEvent(
            id = id,
            startTime = startTime,
            endTime = endTime,
            eventTitle = event.title,
            eventId = event.eventId,
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
