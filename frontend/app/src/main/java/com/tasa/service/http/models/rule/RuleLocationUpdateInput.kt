package com.tasa.service.http.models.rule

import java.time.LocalDateTime

data class RuleLocationUpdateInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
