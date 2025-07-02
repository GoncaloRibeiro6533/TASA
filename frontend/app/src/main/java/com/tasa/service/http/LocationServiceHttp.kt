package com.tasa.service.http

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.service.http.models.location.LocationList
import com.tasa.service.http.utils.delete
import com.tasa.service.http.utils.get
import com.tasa.service.http.utils.post
import com.tasa.service.interfaces.LocationService
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import io.ktor.client.HttpClient

class LocationServiceHttp(private val client: HttpClient) : LocationService {
    override suspend fun fetchLocations(): Either<ApiError, List<Location>> {
        return when (val response = client.get<LocationList>("/location/all")) {
            is Success -> success(response.value.locations)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchLocationById(id: Int): Either<ApiError, Location> {
        return when (val response = client.get<Location>("/location/$id")) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchLocationByName(name: String): Either<ApiError, Location?> {
        TODO("Not yet implemented")
    }

    override suspend fun insertLocation(location: Location): Either<ApiError, Location> {
        return when (
            val response =
                client.post<Location>(
                    "/location/create",
                    body = location.toLocationInput(),
                )
        ) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertLocations(locations: List<Location>): Either<ApiError, List<Location>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteLocationById(id: Int): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/location/remove/$id")) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun deleteLocationByName(name: String): Either<ApiError, Unit> {
        TODO("Not yet implemented")
    }
}
