package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.service.http.models.user.LoginOutput
import com.tasa.service.http.models.user.TokenExternalInfo
import com.tasa.service.interfaces.UserService
import com.tasa.utils.Either
import com.tasa.utils.failure
import com.tasa.utils.success
import java.time.LocalDateTime

class UserServiceFake : UserService {
    companion object {
        private val users =
            mutableListOf<User>(
                User(
                    id = 1,
                    username = "Bob",
                    email = "bob@example.com",
                ),
            )
        private var currentId = 1

        private val passwords =
            mutableMapOf<Int, String>(
                1 to "Strong_password123",
            )

        private val tokens =
            mutableMapOf<Int, String>(
                1 to "token",
            )
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Either<ApiError, LoginOutput> {
        val user =
            users.find { it.username == username }
                ?: return failure(ApiError("User not found"))
        if (passwords[user.id] != password) {
            return failure(ApiError("Invalid password"))
        }
        val token = tokens[user.id] ?: return failure(ApiError("Token not found"))
        return success(
            LoginOutput(
                user,
                TokenExternalInfo(
                    token = token,
                    refreshToken = token,
                    expiration = LocalDateTime.now().plusDays(1),
                ),
            ),
        )
    }

    override suspend fun register(
        username: String,
        password: String,
        email: String,
    ): Either<ApiError, User> {
        if (users.any { it.username == username }) {
            return Either.Left(ApiError("Username already exists"))
        }
        val user =
            User(
                id = currentId++,
                username = username,
                email = email,
            )
        users.add(user)
        passwords[user.id] = password
        return success(user)
    }

    override suspend fun findUserById(
        id: Int,
        token: String,
    ): Either<ApiError, User> {
        val user = users.find { it.id == id } ?: return Either.Left(ApiError("User not found"))
        return success(user)
    }

    override suspend fun logout(token: String): Either<ApiError, Unit> {
        tokens.clear()
        return success(Unit)
    }

    override suspend fun refreshToken(
        token: String,
        refreshToken: String,
    ): Either<ApiError, LoginOutput> {
        val user = users.find { tokens[it.id] == token } ?: return failure(ApiError("Invalid token"))
        val newToken = "new_token_for_${user.id}"
        tokens[user.id] = newToken
        return success(
            LoginOutput(
                user,
                TokenExternalInfo(
                    token = newToken,
                    refreshToken = newToken,
                    expiration = LocalDateTime.now().plusDays(1),
                ),
            ),
        )
    }
}
