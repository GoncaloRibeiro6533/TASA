package com.tasa

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.LocationServices
import com.tasa.activity.UserActivityTransitionManager
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.UserInfoRepository
import com.tasa.geofence.GeofenceManager
import com.tasa.infrastructure.UserInfoRepo
import com.tasa.location.LocationUpdatesRepository
import com.tasa.repository.TasaRepo
import com.tasa.service.TasaService
import com.tasa.service.fake.TasaServiceFake
import com.tasa.service.http.TasaServiceHttp
import com.tasa.storage.TasaDB
import com.tasa.utils.PropertiesConfigLoader
import com.tasa.workers.CoroutineDBCleaner
import com.tasa.workers.LocationStatusWorker
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

class TasaApplication : Application(), DependenciesContainer {
    override val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
        }
    }

    override val preferencesDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "preferences")

    override val userInfoRepository: UserInfoRepository by lazy {
        UserInfoRepo(preferencesDataStore)
    }
    override val clientDB: TasaDB by lazy {
        Room.databaseBuilder(
            context = applicationContext,
            klass = TasaDB::class.java,
            "tasa-db",
        ).fallbackToDestructiveMigration(false).build()
    }

    private val fakeService: TasaServiceFake by lazy {
        TasaServiceFake()
    }

    private val httpService: TasaServiceHttp by lazy {
        TasaServiceHttp(client)
    }

    override val service: TasaService by lazy {
        if (devMode) {
            fakeService
        } else {
            httpService
        }
    }

    override val repo: TasaRepo by lazy {
        TasaRepo(
            local = clientDB,
            remote = service,
        )
    }

    override val ruleScheduler: AlarmScheduler by lazy {
        AlarmScheduler(repo)
    }

    override val activityTransitionManager: UserActivityTransitionManager by lazy {
        UserActivityTransitionManager(this)
    }

    override val geofenceManager: GeofenceManager by lazy {
        GeofenceManager(this, repo)
    }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override val locationUpdatesRepository: LocationUpdatesRepository by lazy {
        LocationUpdatesRepository(activityTransitionManager, fusedLocationClient, userInfoRepository)
    }

    override fun onCreate() {
        super.onCreate()
        val props = PropertiesConfigLoader.load(this@TasaApplication)
        apiUrl = props.getProperty("api_url")
        devMode = props.getProperty("developer_mode", "false").toBoolean()
        val port = props.getProperty("port").toIntOrNull()
        if (port != null) apiUrl = "$apiUrl:$port"
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .build()
        // Schedule the database cleaner to run periodically every day
        val workItem =
            PeriodicWorkRequestBuilder<CoroutineDBCleaner>(
                repeatInterval = 1,
                TimeUnit.DAYS,
            ).setConstraints(constraints).build()
        WorkManager.getInstance(this)
            .enqueue(workItem)
        // Schedule the location status worker to check GPS status every 15 minutes
        // Not at night time
        val constraints2 =
            Constraints.Builder()
                .build()
        // Check for location on/off
        val gpsStatusItem =
            PeriodicWorkRequestBuilder<LocationStatusWorker>(
                repeatInterval = 15,
                TimeUnit.MINUTES,
            ).setConstraints(constraints2).build()
        WorkManager.getInstance(this)
            .enqueue(gpsStatusItem)
    }

    companion object {
        lateinit var apiUrl: String
        var devMode: Boolean = false
    }
}
