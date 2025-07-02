package com.tasa.repository

import com.tasa.domain.ApiError
import com.tasa.domain.UserInfoRepository
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val local: TasaDB,
    private val remote: TasaService,
    private val userInfoRepository: UserInfoRepository,
) : UserRepositoryInterface {
    override suspend fun createUser(
        username: String,
        email: String,
        password: String,
    ): Either<ApiError, User> {
        val result = remote.userService.register(username, email, password)
        return when (result) {
            is Success -> {
                success(result.value)
            }
            is Failure -> failure(result.value)
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
        val result = remote.userService.login(email, password)
        return when (result) {
            is Success -> {
                val userEntity = result.value.user.toUserEntity()
                local.userDao().insertUser(userEntity)
                userInfoRepository.setToken(result.value.session.token)
                userInfoRepository.saveRefreshToken(result.value.session.refreshToken)
                userInfoRepository.setSessionExpiration(result.value.session.expiration)
                userInfoRepository.updateUserInfo(result.value.user)
                success(result.value.user)
            }
            is Failure -> failure(result.value)
        }
    }

    override suspend fun logout(): Either<ApiError, Unit> {
        val token =
            userInfoRepository.getToken()
                ?: return failure(ApiError("User is not authenticated. Please log in again."))

        val result = remote.userService.logout(token)
        return when (result) {
            is Success -> {
                userInfoRepository.clearUserInfo()
                local.userDao().clear()
                success(Unit)
            }
            is Failure -> failure(result.value)
        }
    }
}
