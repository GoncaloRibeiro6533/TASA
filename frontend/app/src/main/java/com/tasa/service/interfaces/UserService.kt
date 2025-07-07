package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.service.http.models.user.LoginOutput
import com.tasa.utils.Either

interface UserService {
    suspend fun login(
        username: String,
        password: String,
    ): Either<ApiError, LoginOutput>

    suspend fun register(
        username: String,
        password: String,
        email: String,
    ): Either<ApiError, User>

    suspend fun findUserById(
        id: Int,
        token: String,
    ): Either<ApiError, User>

    suspend fun logout(token: String): Either<ApiError, Unit>

    suspend fun refreshToken(
        token: String,
        refreshToken: String,
    ): Either<ApiError, LoginOutput>
}
