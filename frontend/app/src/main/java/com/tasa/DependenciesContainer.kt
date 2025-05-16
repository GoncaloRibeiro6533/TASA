package com.tasa

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tasa.service.mock.repo.UserRepoMock

interface DependenciesContainer {
    // TODO remove
    val userRepo: UserRepoMock
    val preferencesDataStore: DataStore<Preferences>
}
