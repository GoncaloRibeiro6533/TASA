package com.tasa.service.http.models.location

import com.tasa.domain.Location

data class LocationList(
    val nLocations: Int,
    val locations: List<Location>,
)
