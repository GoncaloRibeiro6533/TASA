package com.tasa.domain

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
}
