package com.tasa.service.http.models.location

import kotlinx.serialization.Serializable

@Serializable
data class LocationList(
    val nlocations: Int,
    val locations: List<LocationOutput>,
)
