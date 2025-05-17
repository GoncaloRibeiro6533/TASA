package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.service.LocationService
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

    override suspend fun fetchLocations(): Either<ApiError, List<Location>> {
        return success(locations)
    }

    override suspend fun fetchLocationById(id: Int): Either<ApiError, Location?> {
        val location = locations.find { it.id == id }
        return if (location != null) {
            success(location)
        } else {
            Either.Left(ApiError("Location not found"))
        }
    }

    override suspend fun fetchLocationByName(name: String): Either<ApiError, Location?> {
        val location = locations.find { it.name == name }
        return if (location != null) {
            success(location)
        } else {
            Either.Left(ApiError("Location not found"))
        }
    }

    override suspend fun insertLocation(location: Location): Either<ApiError, Location> {
        val newLocation = location.copy(id = currentId++)
        if (locations.any { it.name == newLocation.name }) {
            return Either.Left(ApiError("Location with this name already exists"))
        }
        locations.add(newLocation)
        return success(newLocation)
    }

    override suspend fun insertLocations(locations: List<Location>): Either<ApiError, List<Location>> {
        Companion.locations.addAll(locations.map { it.copy(id = currentId++) })
        return success(Companion.locations.filter { it.id in locations.map { it.id } })
    }

    override suspend fun deleteLocationById(id: Int): Either<ApiError, Unit> {
        val location = locations.find { it.id == id }
        return if (location != null) {
            locations.remove(location)
            success(Unit)
        } else {
            Either.Left(ApiError("Location not found"))
        }
    }

    override suspend fun deleteLocationByName(name: String): Either<ApiError, Unit> {
        val location = locations.find { it.name == name }
        return if (location != null) {
            locations.remove(location)
            success(Unit)
        } else {
            Either.Left(ApiError("Location not found"))
        }
    }
}
