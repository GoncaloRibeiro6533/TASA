package pt.isel.location

import pt.isel.Location
import pt.isel.User

/**
 * Interface for a repository that manages locations.
 */
interface LocationRepository {
    fun create(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        user: User,
    ): Location

    fun findById(id: Int): Location?

    fun findAll(): List<Location>

    fun findByUserId(user: User): List<Location>

    fun updateRadius(
        location: Location,
        radius: Double,
    ): Location

    fun updateName(
        location: Location,
        name: String,
    ): Location

    fun updateCoordinates(
        location: Location,
        latitude: Double,
        longitude: Double,
    ): Location

    fun delete(location: Location): Boolean

    fun clear()
}
