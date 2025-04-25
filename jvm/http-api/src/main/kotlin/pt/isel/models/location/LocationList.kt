package pt.isel.models.location

import pt.isel.Location

data class LocationList(
    val nLocations: Int,
    val locations: List<Location>,
)
