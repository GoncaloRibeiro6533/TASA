package com.tasa.domain

import com.tasa.domain.user.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface UserInfoRepository {
    val userInfo: Flow<User?>

    suspend fun getUserInfo(): User?

    suspend fun updateUserInfo(userInfo: User)

    suspend fun clearUserInfo()

    val lastActivity: Flow<Int?>

    suspend fun writeLastActivity(activity: Int)

    suspend fun getLastActivity(): Int?

    val lastActivityTransition: Flow<Int?>

    val notifiedOfNoLocation: Flow<Boolean>

    suspend fun setNotifiedOfNoLocation(notified: Boolean)

    val locationStatus: Flow<Boolean>

    suspend fun setLocationStatus(enabled: Boolean)

    suspend fun getLocationStatus(): Boolean?

    suspend fun getToken(): String?

    suspend fun setToken(token: String)

    suspend fun saveRefreshToken(token: String)

    suspend fun getRefreshToken(): String?

    suspend fun getSessionExpiration(): LocalDateTime?

    suspend fun setSessionExpiration(expiration: LocalDateTime)

    suspend fun setLocal(local: Boolean)

    suspend fun isLocal(): Boolean
}
