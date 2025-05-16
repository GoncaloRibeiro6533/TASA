package com.tasa

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tasa.domain.UserInfoRepository
import com.tasa.service.UserService

interface DependenciesContainer {
    // TODO remove
    val userService: UserService
    val preferencesDataStore: DataStore<Preferences>
    val userInfoRepository: UserInfoRepository
}
