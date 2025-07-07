package com.tasa.repository

import com.tasa.domain.Location
import com.tasa.domain.RuleLocationTimeless
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.interfaces.Geofence
import com.tasa.repository.interfaces.GeofenceRepositoryInterface
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.localMode.GeofenceLocal
import com.tasa.storage.entities.remote.GeofenceRemote

class GeofenceRepository(
    private val local: TasaDB,
    private val userInfoRepository: UserInfoRepository,
) : GeofenceRepositoryInterface {
    override suspend fun getGeofenceById(id: Int): Geofence? {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getGeofenceById(id)?.toGeofence()
        } else {
            local.remoteDao().getGeofenceById(id)?.toGeofence()
        }
    }

    override suspend fun getAllGeofences(): List<Geofence> {
        return if (userInfoRepository.isLocal()) {
            local.localDao().getAllGeofences().map { it.toGeofence() }
        } else {
            local.remoteDao().getAllGeofences().map { it.toGeofence() }
        }
    }

    override suspend fun createGeofence(
        location: Location,
        rule: RuleLocationTimeless,
    ): Int {
        return if (userInfoRepository.isLocal()) {
            local.localDao().insertGeofenceLocal(
                GeofenceLocal(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = location.radius,
                    name = location.name,
                    ruleId = rule.id,
                ),
            ).toInt()
        } else {
            local.remoteDao().insertGeofenceRemote(
                GeofenceRemote(
                    ruleId = rule.id,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    radius = location.radius,
                    name = location.name,
                ),
            ).toInt()
        }
    }

    override suspend fun updateGeofence(
        geofenceEntity: Geofence,
        location: Location,
    ) {
        if (userInfoRepository.isLocal()) {
            local.localDao().updateGeofenceLocal(
                id = geofenceEntity.id,
                latitude = geofenceEntity.latitude,
                longitude = geofenceEntity.longitude,
                radius = geofenceEntity.radius,
            )
        } else {
            local.remoteDao().updateGeofenceRemote(
                id = geofenceEntity.id,
                latitude = geofenceEntity.latitude,
                longitude = geofenceEntity.longitude,
                radius = geofenceEntity.radius,
            )
        }
    }

    override suspend fun deleteGeofence(geofenceEntity: Geofence) {
        if (userInfoRepository.isLocal()) {
            local.localDao().deleteGeofenceLocalById(geofenceEntity.id)
        } else {
            local.remoteDao().deleteGeofenceRemoteById(geofenceEntity.id)
        }
    }

    override suspend fun clear() {
        if (userInfoRepository.isLocal()) {
            local.localDao().clearGeofences()
        } else {
            local.remoteDao().clearGeofences()
        }
    }
}
