package pt.isel.models.location

data class LocationInput(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
)
