package com.tasa.service.http.models.location

import kotlinx.serialization.Serializable

@Serializable
data class LocationInput(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
)
