package com.tasa.service.http.models.rule

import com.tasa.domain.Event
import com.tasa.domain.RuleEvent
import java.time.LocalDateTime

data class RuleEventOutput(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val event: Event,
) {
    fun toRuleEvent(): RuleEvent {
        return RuleEvent(
            id = id,
            startTime = startTime,
            endTime = endTime,
            event = event,
        )
    }
}
