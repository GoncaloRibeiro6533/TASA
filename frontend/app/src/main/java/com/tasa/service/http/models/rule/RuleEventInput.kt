package com.tasa.service.http.models.rule

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class RuleEventInput(
    val eventId: Int,
    @Contextual
    val startTime: LocalDateTime,
    @Contextual
    val endTime: LocalDateTime,
)
