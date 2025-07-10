package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow

interface LocationRepositoryInterface {
    suspend fun fetchLocations(): Either<ApiError, Flow<List<Location>>>

    suspend fun getLocationByName(name: String): Location?

    suspend fun insertLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Either<ApiError, Location>

    suspend fun deleteLocationById(id: Int): Either<ApiError, Unit>

    suspend fun deleteLocation(location: Location): Either<ApiError, Unit>

    suspend fun updateLocation(location: Location)//: Either<ApiError, Unit>

    suspend fun updateLocationFields(
        name: String,
        radius: Double,
        location: Location,
    )

    suspend fun clear()

    suspend fun syncLocations(): Either<ApiError, Unit>
}
