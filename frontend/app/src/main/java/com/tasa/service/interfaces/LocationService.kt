package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.service.http.models.location.LocationOutput
import com.tasa.utils.Either

interface LocationService {
    suspend fun fetchLocations(token: String): Either<ApiError, List<LocationOutput>>

    suspend fun fetchLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, LocationOutput>

    suspend fun insertLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        token: String,
    ): Either<ApiError, LocationOutput>

    suspend fun deleteLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit>
}
