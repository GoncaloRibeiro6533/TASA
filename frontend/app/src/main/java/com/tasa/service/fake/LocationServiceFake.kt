package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.service.http.models.location.LocationOutput
import com.tasa.service.interfaces.LocationService
import com.tasa.utils.Either
import com.tasa.utils.success

class LocationServiceFake : LocationService {
    companion object {
        private var currentId = 1

        private val locations =
            mutableListOf(
                Location(
                    id = 1,
                    name = "ISEL",
                    latitude = 38.736946,
                    longitude = -9.142685,
                    radius = 100.0,
                ),
            )
    }

    override suspend fun fetchLocations(token: String): Either<ApiError, List<LocationOutput>> {
        return success(
            locations.map {
                LocationOutput(
                    id = it.id,
                    name = it.name,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    radius = it.radius,
                )
            },
        )
    }

    override suspend fun fetchLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, LocationOutput> {
        val location = locations.find { it.id == id }
        return if (location != null) {
            success(
                LocationOutput(
                    id = location.id,
                    name = location.name,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = location.radius,
                ),
            )
        } else {
            Either.Left(ApiError("Location not found"))
        }
    }

    override suspend fun insertLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
        token: String,
    ): Either<ApiError, LocationOutput> {
        val newLocation =
            Location(
                id = currentId++,
                name = name,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
            )
        if (locations.any { it.name == newLocation.name }) {
            return Either.Left(ApiError("Location with this name already exists"))
        }
        locations.add(newLocation)
        return success(
            LocationOutput(
                id = newLocation.id,
                name = newLocation.name,
                latitude = newLocation.latitude,
                longitude = newLocation.longitude,
                radius = newLocation.radius,
            ),
        )
    }

    override suspend fun deleteLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Unit> {
        val location = locations.find { it.id == id }
        return if (location != null) {
            locations.remove(location)
            success(Unit)
        } else {
            Either.Left(ApiError("Location not found"))
        }
    }
}
