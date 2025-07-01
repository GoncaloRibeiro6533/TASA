package com.tasa.service.http.models.rule

import java.time.LocalDateTime

data class RuleEventInput(
    val eventId: Long,
    val calendarId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
