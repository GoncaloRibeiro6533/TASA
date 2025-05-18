package com.tasa

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.tasa.alarm.AlarmScheduler
import com.tasa.domain.UserInfoRepository
import com.tasa.infrastructure.UserInfoRepo
import com.tasa.repository.TasaRepo
import com.tasa.service.TasaService
import com.tasa.service.UserService
import com.tasa.service.fake.TasaServiceFake
import com.tasa.service.fake.UserServiceFake
import com.tasa.storage.TasaDB

class TasaApplication : Application(), DependenciesContainer {
    override val userService: UserService by lazy {
        UserServiceFake()
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
    override val service: TasaService by lazy {
        TasaServiceFake()
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
}
