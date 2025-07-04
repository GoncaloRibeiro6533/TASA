package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow

interface LocationRepositoryInterface {
    suspend fun fetchLocations(): Flow<List<Location>>

    suspend fun fetchLocationById(id: Int): Either<ApiError, Flow<Location?>>

    suspend fun getLocationByName(name: String): Location?

    suspend fun insertLocation(location: Location): Either<ApiError, Location>

    suspend fun deleteLocationById(id: Int)

    suspend fun deleteLocation(location: Location): Either<ApiError, Unit>

    suspend fun clear()
}
