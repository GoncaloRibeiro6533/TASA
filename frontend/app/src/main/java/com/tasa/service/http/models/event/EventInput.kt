package com.tasa.service.http.models.event

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class EventInput(
    val title: String,
    @Contextual
    val startTime: LocalDateTime,
    @Contextual
    val endTime: LocalDateTime,
)
