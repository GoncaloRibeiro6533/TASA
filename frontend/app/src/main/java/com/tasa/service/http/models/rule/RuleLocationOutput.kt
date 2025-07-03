package com.tasa.service.http.models.rule

import com.tasa.domain.Location
import com.tasa.domain.RuleLocationTimeless
import kotlinx.serialization.Serializable

@Serializable
data class RuleLocationOutput(
    val id: Int,
    val location: Location,
) {
    fun toRuleLocationTimeless(): RuleLocationTimeless {
        return RuleLocationTimeless(
            id = id,
            location = location,
        )
    }
}
