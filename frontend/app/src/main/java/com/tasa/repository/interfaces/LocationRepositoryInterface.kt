package com.tasa.repository.interfaces

import com.tasa.domain.Location
import kotlinx.coroutines.flow.Flow

interface LocationRepositoryInterface {
    suspend fun fetchLocations(): Flow<List<Location>>

    suspend fun fetchLocationById(id: Int): Flow<Location?>

    suspend fun fetchLocationByName(name: String): Flow<Location?>

    suspend fun getLocationByName(name: String): Location?

    suspend fun insertLocation(location: Location)

    suspend fun insertLocations(locations: List<Location>)

    suspend fun deleteLocationById(id: Int)

    suspend fun deleteLocationByName(name: String)

    suspend fun clear()
}
