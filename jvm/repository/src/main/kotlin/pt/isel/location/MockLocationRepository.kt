package pt.isel.location

import pt.isel.Location
import pt.isel.User
import kotlin.collections.get

class MockLocationRepository : LocationRepository {
    private var currentId = 0
    private val locations = mutableMapOf<Int, MutableList<Location>>()

    override fun create(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        user: User,
    ): Location {
        val location =
            Location(
                id = ++currentId,
                name = name,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
            )
        locations.computeIfAbsent(user.id) { mutableListOf() }.add(location)
        return location
    }

    override fun findById(id: Int): Location? {
        return locations.values.flatten().find { it.id == id }
    }

    override fun findAll(): List<Location> {
        return locations.values.flatten()
    }

    override fun findByUserId(user: User): List<Location> {
        return locations[user.id]?.toList() ?: emptyList()
    }

    override fun updateRadius(
        location: Location,
        radius: Double,
    ): Location {
        val user = locations.entries.find { it.value.contains(location) }?.key
        locations[user]?.remove(location)
        val updatedLocation = location.copy(radius = radius)
        locations[user]?.add(updatedLocation)
        return updatedLocation
    }

    override fun updateName(
        location: Location,
        name: String,
    ): Location {
        val user = locations.entries.find { it.value.contains(location) }?.key
        locations[user]?.remove(location)
        val updatedLocation = location.copy(name = name)
        locations[user]?.add(updatedLocation)
        return updatedLocation
    }

    override fun updateCoordinates(
        location: Location,
        latitude: Double,
        longitude: Double,
    ): Location {
        val user = locations.entries.find { it.value.contains(location) }?.key
        locations[user]?.remove(location)
        val updatedLocation = location.copy(latitude = latitude, longitude = longitude)
        locations[user]?.add(updatedLocation)
        return updatedLocation
    }

    override fun delete(location: Location): Boolean {
        val user = locations.entries.find { it.value.contains(location) }?.key
        return locations[user]?.remove(location) == true
    }

    override fun clear() {
        locations.clear()
        currentId = 0
    }
}
