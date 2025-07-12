package com.tasa

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.tasa.TasaApplication.Companion.devMode
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
import com.tasa.service.http.models.LocalDateTimeSerializer
import com.tasa.storage.TasaDB
import com.tasa.utils.DefaultStringResourceResolver
import com.tasa.utils.NetworkChecker
import com.tasa.utils.QueryCalendarService
import com.tasa.utils.QueryCalendarServiceImpl
import com.tasa.utils.SearchPlaceService
import com.tasa.utils.SearchPlaceServiceImpl
import com.tasa.utils.ServiceKiller
import com.tasa.utils.ServiceKillerImpl
import com.tasa.utils.StringResourceResolver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDateTime

class TasaTestApplication() : Application(), DependenciesContainer {
    override val client: HttpClient by lazy {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                        serializersModule =
                            SerializersModule {
                                contextual(LocalDateTime::class, LocalDateTimeSerializer)
                            }
                    },
                )
            }
        }
    }

    override val preferencesDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "test_preferences")

    override val userInfoRepository: UserInfoRepository by lazy {
        UserInfoRepo(preferencesDataStore)
    }
    override val clientDB: TasaDB by lazy {
        Room.databaseBuilder(
            context = applicationContext,
            klass = TasaDB::class.java,
            "test-db",
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

    override val ruleScheduler: AlarmScheduler by lazy {
        AlarmScheduler(applicationContext)
    }

    override val activityTransitionManager: UserActivityTransitionManager by lazy {
        UserActivityTransitionManager(applicationContext)
    }

    override val geofenceManager: GeofenceManager by lazy {
        GeofenceManager(applicationContext, fusedLocationClient)
    }

    override val repo: TasaRepo by lazy {
        TasaRepo(
            local = clientDB,
            remote = service,
            userInfoRepository = userInfoRepository,
            ruleScheduler = ruleScheduler,
            geofenceManager = geofenceManager,
            queryCalendarService = queryCalendarService,
            networkChecker = networkChecker,
            serviceKiller = serviceKiller,
        )
    }

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override val locationUpdatesRepository: LocationUpdatesRepository by lazy {
        LocationUpdatesRepository(activityTransitionManager, fusedLocationClient, userInfoRepository)
    }

    override val searchPlaceService: SearchPlaceService by lazy {
        SearchPlaceServiceImpl(applicationContext)
    }

    override val queryCalendarService: QueryCalendarService by lazy {
        QueryCalendarServiceImpl(contentResolver)
    }

    override val serviceKiller: ServiceKiller by lazy {
        ServiceKillerImpl(applicationContext)
    }

    override val stringResourceResolver: StringResourceResolver by lazy {
        DefaultStringResourceResolver(applicationContext)
    }

    override val networkChecker: NetworkChecker by lazy {
        NetworkChecker(applicationContext)
    }
}
