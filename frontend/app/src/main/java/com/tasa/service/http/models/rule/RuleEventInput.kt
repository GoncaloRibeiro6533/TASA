package com.tasa.service.http.models.rule

import java.time.LocalDateTime

data class RuleEventInput(
    val eventId: Int,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
