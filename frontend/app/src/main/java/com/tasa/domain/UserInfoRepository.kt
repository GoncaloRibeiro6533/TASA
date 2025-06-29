package com.tasa.domain

import com.tasa.domain.user.User
import kotlinx.coroutines.flow.Flow

interface UserInfoRepository {
    val userInfo: Flow<User?>

    suspend fun getUserInfo(): User?

    suspend fun updateUserInfo(userInfo: User)

    suspend fun clearUserInfo()

    suspend fun writeLastMode(mode: Mode)

    suspend fun lastMode(): Mode?

    suspend fun writeLanguage(language: Language)

    suspend fun getLanguage(): Language?

    val lastActivity: Flow<Int?>

    suspend fun writeLastActivity(activity: Int)

    suspend fun getLastActivity(): Int?

    val lastActivityTransition: Flow<Int?>

    suspend fun writeLastActivityTransition(transitionType: Int)

    suspend fun getLastActivityTransition(): Int?

    val notifiedOfNoLocation: Flow<Boolean>

    suspend fun setNotifiedOfNoLocation(notified: Boolean)

    val locationStatus: Flow<Boolean>

    suspend fun setLocationStatus(enabled: Boolean)

    suspend fun getLocationStatus(): Boolean?
}
