package com.tasa.service.http.models.rule

import java.time.LocalDateTime

data class RuleEventUpdateInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
