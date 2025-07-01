package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.utils.Either

interface LocationService {
    suspend fun fetchLocations(): Either<ApiError, List<Location>>

    suspend fun fetchLocationById(id: Int): Either<ApiError, Location?>

    suspend fun fetchLocationByName(name: String): Either<ApiError, Location?>

    suspend fun insertLocation(location: Location): Either<ApiError, Location>

    suspend fun insertLocations(locations: List<Location>): Either<ApiError, List<Location>>

    suspend fun deleteLocationById(id: Int): Either<ApiError, Unit>

    suspend fun deleteLocationByName(name: String): Either<ApiError, Unit>
}
