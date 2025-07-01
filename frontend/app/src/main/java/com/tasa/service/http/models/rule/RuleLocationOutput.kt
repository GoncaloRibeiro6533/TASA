package com.tasa.service.http.models.rule

import com.tasa.domain.Location
import com.tasa.domain.RuleLocation
import java.time.LocalDateTime

data class RuleLocationOutput(
    val id: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val location: Location,
) {
    fun toRuleLocation(): RuleLocation {
        return RuleLocation(
            id = id,
            startTime = startTime,
            endTime = endTime,
            location = location,
        )
    }
}
