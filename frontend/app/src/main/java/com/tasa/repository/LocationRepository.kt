package com.tasa.repository

import com.tasa.domain.Location
import com.tasa.domain.TasaException
import com.tasa.repository.interfaces.LocationRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.Failure
import com.tasa.utils.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocationRepository(
    private val local: TasaDB,
    private val remote: TasaService,
): LocationRepositoryInterface {

    private suspend fun hasLocations(): Boolean {
        return local.locationDao().hasLocations()
    }

    private suspend fun getFromApi() =
        remote.locationService.fetchLocations()

    override suspend fun fetchLocations(): Flow<List<Location>> {
        return if (hasLocations()) {
            local.locationDao().getAllLocations().map { it.map { it.toLocation() } }
        } else {
            when(val locations = getFromApi()){
                is Success -> {
                    local.locationDao().insertLocations(locations.value.map{it.toEntity()})
                    local.locationDao().getAllLocations().map { it.map { it.toLocation() } }
                }
                is Failure -> {
                    throw  TasaException(locations.value.message, null)
                }
            }
        }
    }

    override suspend fun fetchLocationById(id: Int): Flow<Location?> {
        return local.locationDao().getLocationById(id).map { it?.toLocation() }
    }

    override suspend fun fetchLocationByName(name: String): Flow<Location?> {
        return local.locationDao().getLocationByName(name).map { it?.toLocation() }
    }

    override suspend fun insertLocation(location: Location) {
        return local.locationDao().insertLocation(location.toEntity())
    }

    override suspend fun insertLocations(locations: List<Location>) {
        return local.locationDao().insertLocations(locations.map { it.toEntity() })
    }

    override suspend fun deleteLocationById(id: Int) {
        local.locationDao().deleteLocationById(id)
    }

    override suspend fun deleteLocationByName(name: String) {
        local.locationDao().deleteLocationByName(name)
    }

    override suspend fun clear() {
        local.locationDao().clear()
    }


}