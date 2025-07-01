package com.tasa.service.http.models.rule

import java.time.LocalDateTime

data class RuleLocationInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val locationId: Int,
)
