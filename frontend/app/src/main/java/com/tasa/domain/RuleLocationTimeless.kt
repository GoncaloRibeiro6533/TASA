package com.tasa.domain

import com.tasa.service.http.models.rule.RuleLocationInput
import com.tasa.storage.entities.RuleLocationTimelessEntity

data class RuleLocationTimeless(
    override val id: Int? = null,
    val location: Location,
) : Rule(id), TimelessRule {
    fun toEntity(): RuleLocationTimelessEntity {
        return RuleLocationTimelessEntity(
            id = id,
            locationName = location.name,
        )
    }

    fun toRuleLocationTimelessInput(id: Int): RuleLocationInput {
        return RuleLocationInput(
            locationId = id,
        )
    }
}
