package com.tasa.domain

import com.tasa.service.http.models.rule.RuleLocationInput
import com.tasa.storage.entities.localMode.RuleLocationLocal
import com.tasa.storage.entities.remote.RuleLocationRemote

data class RuleLocationTimeless(
    override val id: Int,
    val location: Location,
) : Rule(id), TimelessRule {
    fun toEntityRemote(): RuleLocationRemote {
        return RuleLocationRemote(
            id = id,
            locationId = location.id,
        )
    }

    fun toRuleLocationInput(): RuleLocationInput {
        return RuleLocationInput(
            locationId = location.id,
        )
    }

    fun toEntityLocal(): RuleLocationLocal {
        return RuleLocationLocal(
            id = id,
            locationId = location.id,
        )
    }

    fun toRuleLocationTimelessInput(id: Int): RuleLocationInput {
        return RuleLocationInput(
            locationId = id,
        )
    }
}
