package com.tasa.service.http.models.location

import com.tasa.domain.Location
import kotlinx.serialization.Serializable

@Serializable
data class LocationList(
    val nlocations: Int,
    val locations: List<Location>,
)
