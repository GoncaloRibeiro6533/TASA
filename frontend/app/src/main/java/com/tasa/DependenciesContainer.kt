package com.tasa

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tasa.activity.UserActivityTransitionManager
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.UserInfoRepository
import com.tasa.geofence.GeofenceManager
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import io.ktor.client.HttpClient

interface DependenciesContainer {
    val client: HttpClient
    val service: TasaService
    val preferencesDataStore: DataStore<Preferences>
    val userInfoRepository: UserInfoRepository
    val clientDB: TasaDB
    val repo: TasaRepo
    val ruleScheduler: AlarmScheduler
    val activityTransitionManager: UserActivityTransitionManager
    val geofenceManager: GeofenceManager
    val locationUpdatesRepository: LocationUpdatesRepository
}
