package com.tasa.repository

import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.Location
import com.tasa.domain.TasaException
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.LocationRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.NetworkChecker
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepository(
    private val local: TasaDB,
    private val remote: TasaService,
    private val userInfoRepository: UserInfoRepository,
    private val networkChecker: NetworkChecker,
) : LocationRepositoryInterface {
    private suspend fun hasLocations(): Boolean {
        return local.locationDao().hasLocations()
    }

    private suspend fun hasLocationById(id: Int): Boolean {
        return local.locationDao().hasLocationById(id)
    }

    private suspend fun getToken(): String {
        return userInfoRepository.getToken() ?: throw AuthenticationException(
            "User is not authenticated. Please log in again.",
            null,
        )
    }

    private suspend fun getFromApi() = remote.locationService.fetchLocations(getToken())

    override suspend fun fetchLocations(): Flow<List<Location>> {
        return if (hasLocations() || userInfoRepository.isLocal() || !networkChecker.isInternetAvailable()) {
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
        return if (hasLocationById(id) || userInfoRepository.isLocal() || !networkChecker.isInternetAvailable()) {
            success(local.locationDao().getLocationById(id).map { it?.toLocation() })
        } else {
            when (val location = remote.locationService.fetchLocationById(id, getToken())) {
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

    override suspend fun getLocationByName(name: String): Location? {
        return local.locationDao().getLocationByNameSync(name)?.toLocation()
    }

    override suspend fun insertLocation(location: Location): Either<ApiError, Location> {
        if (userInfoRepository.isLocal()) {
            local.locationDao().insertLocation(location.toEntity())
            return success(location)
        }
        val remote = remote.locationService.insertLocation(location, getToken())
        when (remote) {
            is Success -> {
                local.locationDao().insertLocation(remote.value.toEntity())
                return success(location)
            }
            is Failure -> {
                return failure(remote.value)
            }
        }
    }

    override suspend fun deleteLocationById(id: Int) {
        val remote = remote.locationService.deleteLocationById(id, getToken())
        when (remote) {
            is Success -> local.locationDao().deleteLocationById(id)
            is Failure -> throw TasaException(remote.value.message, null)
        }
    }

    override suspend fun deleteLocation(location: Location): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal() || !networkChecker.isInternetAvailable()) {
            local.locationDao().deleteLocationByName(location.name)
            return success(Unit)
        }
        if (location.id != null) {
            val remote = remote.locationService.deleteLocationById(location.id, getToken())
            return when (remote) {
                is Success -> {
                    local.locationDao().deleteLocationById(location.id)
                    success(Unit)
                }
                is Failure -> failure(remote.value)
            }
        } else {
            local.locationDao().deleteLocationByName(location.name)
            return success(Unit)
        }
    }

    override suspend fun clear() {
        local.locationDao().clear()
    }

    override suspend fun syncLocations(): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal()) return success(Unit)
        when (val locations = getFromApi()) {
            is Success -> {
                local.locationDao().insertLocations(locations.value.map { it.toEntity() })
            }
            is Failure -> {
                return failure(locations.value)
            }
        }
        return success(Unit)
    }
}
