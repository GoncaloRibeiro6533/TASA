package com.tasa.repository

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.domain.TasaException
import com.tasa.repository.interfaces.LocationRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepository(
    private val local: TasaDB,
    private val remote: TasaService,
) : LocationRepositoryInterface {
    private suspend fun hasLocations(): Boolean {
        return local.locationDao().hasLocations()
    }

    private suspend fun hasLocationById(id: Int): Boolean {
        return local.locationDao().hasLocationById(id)
    }

    private suspend fun getFromApi() = remote.locationService.fetchLocations()

    override suspend fun fetchLocations(): Flow<List<Location>> {
        return if (hasLocations()) {
            local.locationDao().getAllLocations().map { it.map { it.toLocation() } }
        } else {
            when (val locations = getFromApi()) {
                is Success -> {
                    local.locationDao().insertLocations(locations.value.map { it.toEntity() })
                    local.locationDao().getAllLocations().map { it.map { it.toLocation() } }
                }
                is Failure -> {
                    throw TasaException(locations.value.message, null)
                }
            }
        }
    }

    override suspend fun fetchLocationById(id: Int): Either<ApiError, Flow<Location?>> {
        return if (hasLocationById(id)) {
            success(local.locationDao().getLocationById(id).map { it?.toLocation() })
        } else {
            when (val location = remote.locationService.fetchLocationById(id)) {
                is Success -> {
                    local.locationDao().insertLocation(location.value.toEntity())
                    success(local.locationDao().getLocationById(id).map { it?.toLocation() })
                }
                is Failure -> {
                    failure(location.value)
                }
            }
        }
    }

    override suspend fun fetchLocationByName(name: String): Flow<Location?> {
        return local.locationDao().getLocationByName(name).map { it?.toLocation() }
    }

    override suspend fun getLocationByName(name: String): Location? {
        return local.locationDao().getLocationByNameSync(name)?.toLocation()
    }

    override suspend fun insertLocation(location: Location) {
        val remote = remote.locationService.insertLocation(location)
        when (remote) {
            is Success -> {
                local.locationDao().insertLocation(remote.value.toEntity())
            }
            is Failure -> {
                throw TasaException(remote.value.message, null)
            }
        }
    }

    override suspend fun insertLocations(locations: List<Location>) {
        return local.locationDao().insertLocations(locations.map { it.toEntity() })
    }

    override suspend fun deleteLocationById(id: Int) {
        val remote = remote.locationService.deleteLocationById(id)
        when (remote) {
            is Success -> local.locationDao().deleteLocationById(id)
            is Failure -> throw TasaException(remote.value.message, null)
        }
    }

    override suspend fun deleteLocationByName(name: String) {
        local.locationDao().deleteLocationByName(name)
    }

    override suspend fun clear() {
        local.locationDao().clear()
    }
}
