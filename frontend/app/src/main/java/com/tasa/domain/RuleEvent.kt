package com.tasa.domain

import com.tasa.service.http.models.rule.RuleEventInput
import com.tasa.service.http.models.rule.RuleEventUpdateInput
import com.tasa.storage.entities.localMode.RuleEventLocal
import com.tasa.storage.entities.remote.RuleEventRemote
import com.tasa.ui.screens.rule.EditRuleActivity.RuleParcelableEvent
import java.time.LocalDateTime

data class RuleEvent(
    override val id: Int,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
    val event: Event,
) : Rule(id), TimedRule {
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

    fun toRuleRemote(): RuleEventRemote {
        return RuleEventRemote(
            id = id,
            startTime = startTime,
            endTime = endTime,
            eventId = event.id,
        )
    }

    fun toRuleLocal(): RuleEventLocal {
        return RuleEventLocal(
            id = id,
            startTime = startTime,
            endTime = endTime,
            externalId = event.id,
        )
    }
}
