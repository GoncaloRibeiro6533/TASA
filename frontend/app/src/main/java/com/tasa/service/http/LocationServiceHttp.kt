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
    override suspend fun fetchLocations(token: String): Either<ApiError, List<Location>> {
        return when (val response = client.get<LocationList>("/location/all", token = token)) {
            is Success -> success(response.value.locations)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun fetchLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Location> {
        return when (val response = client.get<Location>("/location/$id", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun insertLocation(
        location: Location,
        token: String,
    ): Either<ApiError, Location> {
        return when (
            val response =
                client.post<Location>(
                    "/location/create",
                    body = location.toLocationInput(),
                    token = token,
                )
        ) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun deleteLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        return when (val response = client.delete<Unit>("/location/remove/$id", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }
}
