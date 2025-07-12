package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.service.http.models.location.LocationOutput
import com.tasa.utils.Either

/**
 * LocationService defines the contract for managing locations in the application.
 * It provides methods to fetch, insert, and delete locations.
 */

interface LocationService {
    /**
     * Fetches all locations of the user.
     * @param token The authentication token for the request.
     * @return Either an ApiError or a list of LocationOutput.
     */
    suspend fun fetchLocations(token: String): Either<ApiError, List<LocationOutput>>

    /**
     * Fetches a location by its ID.
     * @param id The ID of the location to be retrieved.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the retrieved LocationOutput.
     */
    suspend fun fetchLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, LocationOutput>

    /**
     * Inserts a new location with the specified parameters.
     * @param name The name of the location.
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @param radius The radius of the location.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the inserted LocationOutput.
     */
    suspend fun insertLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        token: String,
    ): Either<ApiError, LocationOutput>

    /**
     * Deletes a location by its ID.
     * @param id The ID of the location to be deleted.
     * @param token The authentication token for the request.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun deleteLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>
}
