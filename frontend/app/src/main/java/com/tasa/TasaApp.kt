package com.tasa

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.tasa.domain.UserInfoRepository
import com.tasa.infrastructure.UserInfoRepo
import com.tasa.service.mock.repo.UserRepoMock

class TasaApplication : Application(), DependenciesContainer {
    override val userRepo: UserRepoMock by lazy {
        UserRepoMock()
    }

    override val preferencesDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "preferences")

    override val userInfoRepository: UserInfoRepository by lazy {
        UserInfoRepo(preferencesDataStore)
    }
}
