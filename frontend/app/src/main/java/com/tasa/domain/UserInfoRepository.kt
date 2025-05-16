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
}
