package com.tasa

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.UserInfoRepository
import com.tasa.geofence.GeofenceManager
import com.tasa.newlocation.UserActivityTransitionManager
import com.tasa.repository.TasaRepo
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB

interface DependenciesContainer {
    val service: TasaService
    val preferencesDataStore: DataStore<Preferences>
    val userInfoRepository: UserInfoRepository
    val clientDB: TasaDB
    val repo: TasaRepo
    val ruleScheduler: AlarmScheduler
    val activityTransitionManager: UserActivityTransitionManager
    val geofenceManager: GeofenceManager
    val locationManager: LocationManager
}
