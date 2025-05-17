package com.tasa

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tasa.domain.UserInfoRepository
import com.tasa.repository.TasaRepo
import com.tasa.service.TasaService
import com.tasa.service.UserService
import com.tasa.storage.TasaDB

interface DependenciesContainer {
    // TODO remove
    val userService: UserService
    val service: TasaService
    val preferencesDataStore: DataStore<Preferences>
    val userInfoRepository: UserInfoRepository
    val clientDB: TasaDB
    val repo: TasaRepo
}
