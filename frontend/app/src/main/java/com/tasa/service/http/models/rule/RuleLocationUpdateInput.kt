package com.tasa.service.http.models.rule

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class RuleLocationUpdateInput(
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
)
