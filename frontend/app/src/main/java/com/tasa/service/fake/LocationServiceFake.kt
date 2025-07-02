package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.Location
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

    override suspend fun fetchLocations(token: String): Either<ApiError, List<Location>> {
        return success(locations)
    }

    override suspend fun fetchLocationById(
        id: Int,
        token: String,
    ): Either<ApiError, Location> {
        val location = locations.find { it.id == id }
        return if (location != null) {
            success(location)
        } else {
            Either.Left(ApiError("Location not found"))
        }
    }

    override suspend fun insertLocation(
        location: Location,
        token: String,
    ): Either<ApiError, Location> {
        val newLocation = location.copy(id = currentId++)
        if (locations.any { it.name == newLocation.name }) {
            return Either.Left(ApiError("Location with this name already exists"))
        }
        locations.add(newLocation)
        return success(newLocation)
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
