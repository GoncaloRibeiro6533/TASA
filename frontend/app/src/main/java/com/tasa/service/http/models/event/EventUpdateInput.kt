package com.tasa.service.http.models.event

import kotlinx.serialization.Serializable

@Serializable
data class EventUpdateInput(
    val newTitle: String,
)
