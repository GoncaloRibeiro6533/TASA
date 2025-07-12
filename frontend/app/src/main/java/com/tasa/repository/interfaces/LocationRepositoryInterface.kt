package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow

/**
 * LocationRepositoryInterface defines the contract for managing locations in the application.
 * It provides methods to fetch, insert, delete, and update locations, as well as synchronize them with a remote source.
 */
interface LocationRepositoryInterface {
    /**
     * Fetches a flow of locations.
     * @return Either an ApiError or a Flow of List of Location.
     */
    suspend fun fetchLocations(): Either<ApiError, Flow<List<Location>>>

    /**
     * Retrieves a location by its name.
     * @param name The name of the location to be retrieved.
     * @return Either a Location or null if not found.
     */
    suspend fun getLocationByName(name: String): Location?

    /**
     * Inserts a new location with the specified parameters.
     * @param name The name of the location.
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @param radius The radius of the location.
     * @return Either an ApiError or the inserted Location.
     */
    suspend fun insertLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Either<ApiError, Location>

    /**
     * Deletes a location by its ID.
     * @param id The ID of the location to be retrieved.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteLocationById(id: Int): Either<ApiError, Unit>

    /**
     * Deletes a location.
     * @param location The location to be deleted.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteLocation(location: Location): Either<ApiError, Unit>

    /**
     * Updates an existing location.
     * @param location The location to be updated.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun updateLocation(location: Location) // : Either<ApiError, Unit>

    /**
     * Updates the fields of a location.
     * @param name The name of the location.
     * @param radius The radius of the location.
     * @param location The Location object containing updated coordinates.
     */
    suspend fun updateLocationFields(
        name: String,
        radius: Double,
        location: Location,
    )

    /**
     * Clears all locations from the repository.
     * @return Unit if successful.
     */
    suspend fun clear()
}
