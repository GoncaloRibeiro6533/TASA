package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.utils.Either

interface LocationService {
    suspend fun fetchLocations(token: String): Either<ApiError, List<Location>>

    suspend fun fetchLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Location>

    suspend fun insertLocation(
        location: Location,
        token: String,
    ): Either<ApiError, Location>

    suspend fun deleteLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>
}
