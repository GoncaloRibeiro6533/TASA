package com.tasa.domain

import com.tasa.service.http.models.rule.RuleLocationInput
import com.tasa.service.http.models.rule.RuleLocationUpdateInput
import com.tasa.storage.entities.RuleLocationEntity
import java.time.LocalDateTime

data class RuleLocation(
    override val id: Int? = null,
    override val startTime: LocalDateTime,
    override val endTime: LocalDateTime,
    val location: Location,
) : Rule(id), TimedRule {
    fun toRuleLocationEntity(): RuleLocationEntity {
        return RuleLocationEntity(
            id = id,
            startTime = startTime,
            endTime = endTime,
            locationName = location.name,
        )
    }

    fun toRuleLocationInput(locationId: Int): RuleLocationInput {
        return RuleLocationInput(
            startTime = startTime,
            endTime = endTime,
            locationId = locationId,
        )
    }

    fun toRuleLocationUpdateInput(): RuleLocationUpdateInput {
        return RuleLocationUpdateInput(
            startTime = startTime,
            endTime = endTime,
        )
    }
}
