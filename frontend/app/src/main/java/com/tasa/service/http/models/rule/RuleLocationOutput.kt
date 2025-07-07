package com.tasa.service.http.models.rule

import com.tasa.domain.RuleLocationTimeless
import com.tasa.service.http.models.location.LocationOutput
import com.tasa.storage.entities.remote.RuleLocationRemote
import kotlinx.serialization.Serializable

@Serializable
data class RuleLocationOutput(
    val id: Int,
    val location: LocationOutput,
) {
    fun toRuleLocationTimeless(): RuleLocationTimeless {
        return RuleLocationTimeless(
            id = id,
            location = location.toLocation(),
        )
    }

    fun toRuleLocationRemote(): RuleLocationRemote {
        return RuleLocationRemote(
            id = id,
            locationId = location.id,
        )
    }
}
