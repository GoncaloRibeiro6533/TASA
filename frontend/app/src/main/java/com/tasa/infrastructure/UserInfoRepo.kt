package com.tasa.infrastructure

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.tasa.domain.Language
import com.tasa.domain.Mode
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.user.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

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

    override suspend fun writeLanguage(language: Language) {
        store.edit { preferences ->
            language.writeToPreferences(preferences)
        }
    }

    override suspend fun getLanguage(): Language? {
        val preferences = store.data.first()
        val languageCode = preferences[LANGUAGE_KEY] ?: return null
        return Language.ALL_LANGUAGES.firstOrNull { it.code == languageCode }
    }

    override suspend fun writeLastActivity(activity: Int) {
        store.edit { preferences ->
            preferences[ACTIVITY_KEY] = activity.toString()
        }
    }

    override suspend fun getLastActivity(): Int? {
        val preferences = store.data.first()
        return preferences[ACTIVITY_KEY]?.toIntOrNull()
    }

    override val lastActivity: Flow<Int?> =
        store.data.map { preferences ->
            preferences[ACTIVITY_KEY]?.toIntOrNull()
        }

    override suspend fun writeLastActivityTransition(transitionType: Int) {
        store.edit { preferences ->
            preferences[TRANSITION_KEY] = transitionType.toString()
        }
    }

    override suspend fun getLastActivityTransition(): Int? {
        val preferences = store.data.first()
        return preferences[TRANSITION_KEY]?.toIntOrNull()
    }

    override val notifiedOfNoLocation: Flow<Boolean>
        get() {
            return store.data.map { preferences ->
                preferences.toNotifiedOfNoLocation() ?: false
            }
        }

    override suspend fun setNotifiedOfNoLocation(notified: Boolean) {
        store.edit { preferences ->
            preferences[NOTIFIES_OF_NO_LOCATION_KEY] = notified.toString()
        }
    }

    override val locationStatus: Flow<Boolean>
        get() {
            return store.data.map { preferences ->
                preferences[LOCATION_STATUS_KEY]?.toBoolean() ?: false
            }
        }

    override suspend fun setLocationStatus(enabled: Boolean) {
        store.edit { preferences ->
            preferences[LOCATION_STATUS_KEY] = enabled.toString()
        }
    }

    override suspend fun getLocationStatus(): Boolean? {
        val preferences = store.data.first()
        return preferences[LOCATION_STATUS_KEY]?.toBoolean()
    }

    override suspend fun getToken(): String? {
        val preferences = store.data.first()
        return preferences[TOKEN_KEY]
    }

    override suspend fun setToken(token: String) {
        store.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    override suspend fun saveRefreshToken(token: String) {
        store.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    override suspend fun getRefreshToken(): String? {
        val preferences = store.data.first()
        return preferences[REFRESH_TOKEN_KEY]
    }

    override suspend fun getSessionExpiration(): LocalDateTime? {
        val preferences = store.data.first()
        val expirationString = preferences[SESSION_EXPIRATION_KEY] ?: return null
        return LocalDateTime.parse(expirationString)
    }

    override suspend fun setSessionExpiration(expiration: LocalDateTime) {
        store.edit { preferences ->
            preferences[SESSION_EXPIRATION_KEY] = expiration.toString()
        }
    }

    override suspend fun isLocal(): Boolean {
        val preferences = store.data.first()
        return preferences[IS_LOCAL]?.toBoolean() ?: false
    }

    override val lastActivityTransition: Flow<Int?> =
        store.data.map { preferences ->
            preferences[TRANSITION_KEY]?.toIntOrNull()
        }

    override suspend fun setLocal(isLocal: Boolean) {
        Log.d("UserInfoRepo", "Setting user info to local: $isLocal")
        store.edit { preferences ->
            preferences[IS_LOCAL] = isLocal.toString()
        }
    }
}

private val USERNAME_KEY = stringPreferencesKey("username")
private val USER_ID_KEY = stringPreferencesKey("userId")
private val USER_EMAIL_KEY = stringPreferencesKey("email")
private val MODE_KEY = stringPreferencesKey("mode")
private val LANGUAGE_KEY = stringPreferencesKey("language")
private val ACTIVITY_KEY = stringPreferencesKey("activity")
private val TRANSITION_KEY = stringPreferencesKey("transition")
private val NOTIFIES_OF_NO_LOCATION_KEY = stringPreferencesKey("notifies_of_no_location")
private val LOCATION_STATUS_KEY = stringPreferencesKey("location_status")
private val TOKEN_KEY = stringPreferencesKey("token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
private val SESSION_EXPIRATION_KEY = stringPreferencesKey("session_expiration")
private val IS_LOCAL = stringPreferencesKey("is_local")

private fun Preferences.toUser(): User? {
    val username = this[USERNAME_KEY] ?: return null
    val userId = this[USER_ID_KEY] ?: return null
    val email = this[USER_EMAIL_KEY] ?: return null
    return User(userId.toInt(), username, email)
}

private fun Preferences.toNotifiedOfNoLocation(): Boolean? {
    return this[NOTIFIES_OF_NO_LOCATION_KEY]?.toBoolean()
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

private fun Language.writeToPreferences(preferences: MutablePreferences): MutablePreferences {
    preferences[LANGUAGE_KEY] = this.code
    return preferences
}

private fun String.writeToPreferences(preferences: MutablePreferences): MutablePreferences {
    preferences[ACTIVITY_KEY] = this
    return preferences
}

private fun Boolean.writeToPreferences(preferences: MutablePreferences): MutablePreferences {
    preferences[NOTIFIES_OF_NO_LOCATION_KEY] = this.toString()
    return preferences
}
