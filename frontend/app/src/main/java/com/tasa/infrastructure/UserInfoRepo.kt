package com.tasa.infrastructure

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tasa.domain.Mode
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserInfoRepo(private val store: DataStore<Preferences>) : UserInfoRepository {
    override val userInfo: Flow<User?> =
        store.data.map { preferences ->
            preferences.toUser()
        }

    override suspend fun getUserInfo(): User? {
        val preferences = store.data.first()
        return preferences.toUser()
    }

    override suspend fun updateUserInfo(userInfo: User) {
        store.edit { preferences ->
            userInfo.writeToPreferences(preferences)
        }
    }

    override suspend fun clearUserInfo() {
        store.edit { it.clear() }
    }

    override suspend fun writeLastMode(mode: Mode) {
        store.edit { preferences ->
            mode.writeToPreferences(preferences)
        }
    }

    override suspend fun lastMode(): Mode? {
        val preferences = store.data.first()
        val modeValue = preferences[MODE_KEY]
        return Mode.entries.firstOrNull { it.value == modeValue }
    }
}

private val USERNAME_KEY = stringPreferencesKey("username")
private val USER_ID_KEY = stringPreferencesKey("userId")
private val USER_EMAIL_KEY = stringPreferencesKey("email")
private val MODE_KEY = stringPreferencesKey("mode")

private fun Preferences.toUser(): User? {
    val username = this[USERNAME_KEY] ?: return null
    val userId = this[USER_ID_KEY] ?: return null
    val email = this[USER_EMAIL_KEY] ?: return null
    return User(userId.toInt(), username, email)
}

private fun User.writeToPreferences(preferences: MutablePreferences): MutablePreferences {
    preferences[USERNAME_KEY] = username
    preferences[USER_ID_KEY] = id.toString()
    preferences[USER_EMAIL_KEY] = email
    return preferences
}

private fun Mode.writeToPreferences(preferences: MutablePreferences): MutablePreferences {
    preferences[MODE_KEY] = value
    return preferences
}
