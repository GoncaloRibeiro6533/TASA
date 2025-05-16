package com.tasa.service.mock

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.utils.Either

interface UserService {
    suspend fun fetchUser(): Either<ApiError, User>

    suspend fun updateUsername(newUsername: String): Either<ApiError, User>

    suspend fun login(
        username: String,
        password: String,
    ): Either<ApiError, User>

    suspend fun register(
        username: String,
        password: String,
        email: String,
    ): Either<ApiError, User>

    suspend fun findUserById(id: Int): Either<ApiError, User>

    suspend fun logout(): Either<ApiError, Unit>

    suspend fun findUserByUsername(query: String): Either<ApiError, List<User>>
}
