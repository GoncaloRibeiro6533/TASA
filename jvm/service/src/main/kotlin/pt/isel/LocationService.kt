package pt.isel

import jakarta.inject.Named
import pt.isel.transaction.TransactionManager

sealed class LocationError {
    data object LocationNotFound : LocationError()

    data object NegativeIdentifier : LocationError()

    data object InvalidLocationName : LocationError()

    data object InvalidLocationCoordinates : LocationError()

    data object NotAllowed : LocationError()

    data object AlreadyExists : LocationError()

    data object UserNotFound : LocationError()

    data object InvalidLocationRadius : LocationError()
}

/**
 * Service that manages locations of users.
 * This includes operations
 * for managing contact and app exclusions, such as creation, retrieval,
 * deletion, and updating of locations.
 *
 * @constructor Initializes the `LocationService` with the provided `TransactionManager`.
 *
 * @param trxManager The transaction manager used to execute operations within a transactional context.
 */
@Named
class LocationService(
    private val trxManager: TransactionManager,
) {
    /**
     * Creates a location for a user.
     *
     * @param userId The ID of the user creating the location.
     * @param name The name of the location.
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @param radius The radius of the location.
     *
     * @return Either an [LocationError] or the created [Location].
     */
    fun createLocation(
        userId: Int,
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Either<LocationError, Location> =
        trxManager.run {
            if (userId < 0) {
                return@run failure(LocationError.NegativeIdentifier)
            }
            if (name.isBlank()) {
                return@run failure(LocationError.InvalidLocationName)
            }
            if (latitude < Location.MIN_LATITUDE || latitude > Location.MAX_LATITUDE) {
                return@run failure(LocationError.InvalidLocationCoordinates)
            }
            if (longitude < Location.MIN_LONGITUDE || longitude > Location.MAX_LONGITUDE) {
                return@run failure(LocationError.InvalidLocationCoordinates)
            }
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(LocationError.UserNotFound)
            if (locationRepo.findByUserId(user).any { it.name == name }) {
                return@run failure(LocationError.AlreadyExists)
            }
            if (radius < 0) {
                return@run failure(LocationError.InvalidLocationRadius)
            }
            success(
                locationRepo.create(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius,
                    user = user,
                ),
            )
        }

    /**
     * Retrieves a location by its ID for a specific user.
     *
     * @param userId The ID of the user.
     * @param id The ID of the location to retrieve.
     *
     * @return Either an [LocationError] or the retrieved [Location].
     * */
    fun getLocationById(
        userId: Int,
        id: Int,
    ): Either<LocationError, Location> =
        trxManager.run {
            if (userId < 0 || id < 0) return@run failure(LocationError.NegativeIdentifier)
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(LocationError.UserNotFound)
            val location =
                locationRepo.findById(id)
                    ?: return@run failure(LocationError.LocationNotFound)
            if (location !in locationRepo.findByUserId(user)) {
                return@run failure(LocationError.NotAllowed)
            }
            success(location)
        }

    /**
     * Retrieves all locations for a specific user.
     *
     * @param userId The ID of the user whose locations are to be retrieved.
     *
     * @return Either an [LocationError] or a list of [Location] objects.
     */
    fun getAllLocations(userId: Int): Either<LocationError, List<Location>> =
        trxManager.run {
            if (userId < 0) return@run failure(LocationError.NegativeIdentifier)
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(LocationError.UserNotFound)
            success(locationRepo.findByUserId(user))
        }

    /**
     * Updates the name of a location.
     *
     * @param userId The ID of the user.
     * @param locationId The ID of the location to update.
     *
     * @return Either an [LocationError] or the updated [Location].
     */
    fun updateLocationName(
        userId: Int,
        locationId: Int,
        name: String,
    ): Either<LocationError, Location> =
        trxManager.run {
            if (userId < 0 || locationId < 0) return@run failure(LocationError.NegativeIdentifier)
            if (name.isBlank()) return@run failure(LocationError.InvalidLocationName)
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(LocationError.UserNotFound)
            val location =
                locationRepo.findById(locationId)
                    ?: return@run failure(LocationError.LocationNotFound)
            if (location !in locationRepo.findByUserId(user)) {
                return@run failure(LocationError.NotAllowed)
            }
            if (locationRepo.findByUserId(user).any { it.name == name }) {
                return@run failure(LocationError.AlreadyExists)
            }
            success(
                locationRepo.updateName(
                    location = location,
                    name = name,
                ),
            )
        }

    /**
     * Updates the name of a location.
     *
     * @param userId The ID of the user.
     * @param locationId The ID of the location to update.
     * @param radius The new radius of the location.
     *
     * @return Either an [LocationError] or the updated [Location].
     */
    fun updateLocationRadius(
        userId: Int,
        locationId: Int,
        radius: Double,
    ): Either<LocationError, Location> =
        trxManager.run {
            if (userId < 0 || locationId < 0) return@run failure(LocationError.NegativeIdentifier)
            if (radius < 0) return@run failure(LocationError.InvalidLocationRadius)
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(LocationError.UserNotFound)
            val location =
                locationRepo.findById(locationId)
                    ?: return@run failure(LocationError.LocationNotFound)
            if (location !in locationRepo.findByUserId(user)) {
                return@run failure(LocationError.NotAllowed)
            }
            success(
                locationRepo.updateRadius(
                    location = location,
                    radius = radius,
                ),
            )
        }

    /**
     * Deletes a location for a user.
     *
     * @param userId The ID of the user.
     * @param locationId The ID of the location to delete.
     *
     * @return Either an [LocationError] or a boolean indicating success.
     */
    fun deleteLocation(
        userId: Int,
        locationId: Int,
    ): Either<LocationError, Boolean> =
        trxManager.run {
            if (userId < 0 || locationId < 0) return@run failure(LocationError.NegativeIdentifier)
            val user =
                userRepo.findById(userId)
                    ?: return@run failure(LocationError.UserNotFound)
            val location =
                locationRepo.findById(locationId)
                    ?: return@run failure(LocationError.LocationNotFound)
            if (location !in locationRepo.findByUserId(user)) {
                return@run failure(LocationError.NotAllowed)
            }
            success(locationRepo.delete(location))
        }
}
