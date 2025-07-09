package com.tasa.repository

import android.util.Log
import com.tasa.domain.ApiError
import com.tasa.domain.AuthenticationException
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.NetworkChecker
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val local: TasaDB,
    private val remote: TasaService,
    private val userInfoRepository: UserInfoRepository,
    private val networkChecker: NetworkChecker,
) : UserRepositoryInterface {
    override suspend fun createUser(
        username: String,
        email: String,
        password: String,
    ): Either<ApiError, User> {
        if (!networkChecker.isInternetAvailable()) {
            throw AuthenticationException(
                "No internet connection. Please check your network settings.",
                null,
            )
        }
        if (userInfoRepository.isLocal()) {
            return failure(ApiError("Local mode is enabled. Cannot create user."))
        } else {
            val result =
                remote.userService.register(
                    username = username,
                    password = password,
                    email = email,
                )
            return when (result) {
                is Success -> {
                    success(result.value)
                }
                is Failure -> failure(result.value)
            }
        }
    }

    override suspend fun insertUser(user: User) {
        local.userDao().insertUser(
            user.toUserEntity(),
        )
    }

    override fun getUsers(): Flow<List<User>> {
        return local.userDao().getAllUsers().map { user ->
            user.map {
                User(
                    it.id,
                    it.username,
                    it.email,
                )
            }
        }
    }

    override suspend fun clear() {
        local.userDao().clear()
    }

    override suspend fun createToken(
        email: String,
        password: String,
    ): Either<ApiError, User> {
        if (!networkChecker.isInternetAvailable()) {
            throw AuthenticationException(
                "No internet connection. Please check your network settings.",
                null,
            )
        }
        if (userInfoRepository.isLocal()) {
            return failure(ApiError("Local mode is enabled. Cannot create session."))
        } else {
            val result = remote.userService.login(email, password)
            return when (result) {
                is Success -> {
                    val userEntity = result.value.user.toUserEntity()
                    local.userDao().insertUser(userEntity)
                    userInfoRepository.setToken(result.value.session.token)
                    userInfoRepository.saveRefreshToken(result.value.session.refreshToken)
                    userInfoRepository.setSessionExpiration(
                        result.value.session.expiration,
                    )
                    userInfoRepository.updateUserInfo(result.value.user)
                    success(result.value.user)
                }
                is Failure -> failure(result.value)
            }
        }
    }

    override suspend fun refreshSession(): Either<ApiError, String> {
        if (!networkChecker.isInternetAvailable()) {
            throw AuthenticationException(
                "No internet connection. Please check your network settings.",
                null,
            )
        }
        if (userInfoRepository.isLocal()) {
            return failure(ApiError("Local mode is enabled. Cannot refresh session."))
        }
        val token = getToken()
        Log.e("ServiceWithRetry", "Token: $token")
        val refreshToken =
            userInfoRepository.getRefreshToken() ?: return failure(
                ApiError("No refresh token available. Please log in again."),
            )
        val result = remote.userService.refreshToken(token, refreshToken)
        return when (result) {
            is Success -> {
                userInfoRepository.setToken(result.value.session.token)
                userInfoRepository.saveRefreshToken(result.value.session.refreshToken)
                userInfoRepository.setSessionExpiration(
                    result.value.session.expiration,
                )
                userInfoRepository.updateUserInfo(result.value.user)
                success(result.value.session.token)
            }
            is Failure -> {
                failure(result.value)
            }
        }
    }

    suspend fun getToken(): String {
        return userInfoRepository.getToken() ?: throw AuthenticationException(
            "User is not authenticated. Please log in again.",
            null,
        )
    }

    override suspend fun logout(): Either<ApiError, Unit> {
        if (userInfoRepository.isLocal()) {
            local.userDao().clear()
            return success(Unit)
        }
        val result = remote.userService.logout(getToken())
        return when (result) {
            is Success -> {
                local.userDao().clear()
                success(Unit)
            }
            is Failure -> failure(result.value)
        }
    }
}
