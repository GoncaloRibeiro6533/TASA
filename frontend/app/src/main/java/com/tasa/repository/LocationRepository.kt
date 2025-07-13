package com.tasa.repository

import com.tasa.domain.ApiError
import com.tasa.domain.Location
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.LocationRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.service.interfaces.ServiceWithRetry
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
    private val userInfoRepository: UserInfoRepository,
    userRepo: UserRepository,
) : LocationRepositoryInterface, ServiceWithRetry(userRepo) {
    private suspend fun hasLocations(): Boolean {
        return if (userInfoRepository.isLocal()) {
            local.localDao().hasLocations()
        } else {
            local.remoteDao().hasLocations()
        }
    }

    private suspend fun getFromApi() =
        retryOnFailure {
                token ->
            remote.locationService.fetchLocations(token)
        }

    override suspend fun fetchLocations(): Either<ApiError, Flow<List<Location>>> {
        return if (userInfoRepository.isLocal()) {
            success(local.localDao().getLocationsFlow().map { it.map { it.toLocation() } })
        } else {
            if (hasLocations()) {
                return success(local.remoteDao().getLocationsFlow().map { it.map { it.toLocation() } })
            }
            when (val locations = getFromApi()) {
                is Success -> {
                    local.remoteDao().insertLocationRemote(
                        *locations.value.map { it.toRemoteLocation() }.toTypedArray(),
                    )
                    success(local.remoteDao().getLocationsFlow().map { it.map { it.toLocation() } })
                }
                is Failure -> {
                    failure(locations.value)
                }
            }
        }
    }

    override suspend fun getLocationByName(name: String): Location? {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getLocationByName(name)?.toLocation()
        } else {
            local.remoteDao().getLocationByName(name)?.toLocation()
        }
    }

    override suspend fun insertLocation(
        name: String,
        latitude: Double,
        longitude: Double,
        radius: Double,
    ): Either<ApiError, Location> {
        if (userInfoRepository.isLocal()) {
            val location =
                Location(
                    id = 0,
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius,
                )
            val id = local.localDao().insertLocationLocal(location.toLocationLocal())
            return success(location.copy(id = id.toInt()))
        }
        val remote =
            retryOnFailure { token ->
                remote.locationService.insertLocation(
                    name = name,
                    latitude = latitude,
                    longitude = longitude,
                    radius = radius,
                    token,
                )
            }
        when (remote) {
            is Success -> {
                local.remoteDao().insertLocationRemote(remote.value.toRemoteLocation())
                return success(remote.value.toLocation())
            }
            is Failure -> {
                return failure(remote.value)
            }
        }
    }

    override suspend fun deleteLocationById(id: Int): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal()) {
            local.localDao().deleteLocationLocalById(id)
            return success(Unit)
        }
        val remote =
            retryOnFailure {
                    token ->
                remote.locationService.deleteLocationById(id, token)
            }
        return when (remote) {
            is Success -> {
                local.remoteDao().deleteLocationRemoteById(id)
                success(Unit)
            }
            is Failure -> failure(remote.value)
        }
    }

    override suspend fun deleteLocation(location: Location): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal()) {
            local.localDao().deleteLocationLocalById(location.id)
            return success(Unit)
        }
        val remote =
            retryOnFailure {
                    token ->
                remote.locationService.deleteLocationById(location.id, token)
            }
        return when (remote) {
            is Success -> {
                local.remoteDao().deleteLocationRemoteById(location.id)
                success(Unit)
            }
            is Failure -> failure(remote.value)
        }
    }

    override suspend fun syncLocations(): Either<ApiError, Unit> {
        when (val result = getFromApi()) {
            is Success -> {
                local.remoteDao().insertLocationRemote(
                    *result.value.map { it.toRemoteLocation() }.toTypedArray(),
                )
                return success(Unit)
            }
            is Failure -> {
                return failure(result.value)
            }
        }
    }

    override suspend fun clear() {
        if (userInfoRepository.isLocal()) {
            local.localDao().clearLocations()
        } else {
            local.remoteDao().clearLocations()
        }
    }
}
