package pt.isel

import java.time.LocalDateTime

/**
* @property startTime the rule's start time
* @property endTime the rule's end time
**/
class RuleEvent(
    id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    creator: User,
    val event: Event,
) : Rule(id, creator) {
    override fun toString(): String {
        return "RuleEvent(id=$id, startTime=$startTime, endTime=$endTime, creator= $creator, event=$event)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RuleEvent) return false
        if (!super.equals(other)) return false
        if (event != other.event) return false
        return true
    }

    fun copy(
        id: Int = this.id,
        startTime: LocalDateTime = this.startTime,
        endTime: LocalDateTime = this.endTime,
        creator: User = this.creator,
        event: Event = this.event,
    ): RuleEvent {
        return RuleEvent(id, startTime, endTime, creator, event)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + event.hashCode()
        return result
    }
}
