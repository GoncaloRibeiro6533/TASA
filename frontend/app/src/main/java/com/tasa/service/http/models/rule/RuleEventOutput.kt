package com.tasa.service.http.models.rule

import com.tasa.domain.Event
import com.tasa.domain.RuleEvent
import com.tasa.service.http.models.event.EventOutput
import com.tasa.storage.entities.remote.RuleEventRemote
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class RuleEventOutput(
    val id: Int,
    @Contextual
    val startTime: LocalDateTime,
    @Contextual
    val endTime: LocalDateTime,
    val event: EventOutput,
) {
    fun toRuleEvent(event: Event): RuleEvent {
        return RuleEvent(
            id = id,
            startTime = startTime,
            endTime = endTime,
            event = event,
        )
    }

    fun toRuleEventRemote(): RuleEventRemote {
        return RuleEventRemote(
            id = id,
            startTime = startTime,
            endTime = endTime,
            eventId = event.id,
        )
    }
}
