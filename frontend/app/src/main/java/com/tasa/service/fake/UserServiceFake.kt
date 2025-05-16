package com.tasa.service.fake

import com.tasa.domain.ApiError
import com.tasa.domain.user.AuthenticatedUser
import com.tasa.domain.user.User
import com.tasa.service.UserService
import com.tasa.utils.Either
import com.tasa.utils.success

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

        private val token =
            mutableMapOf<Int, String>(
                1 to "token",
            )
    }

    override suspend fun fetchUser(): Either<ApiError, User> {
        return success(users[0])
    }

    override suspend fun updateUsername(newUsername: String): Either<ApiError, User> {
        val result = users[0].copy(username = newUsername)
        users[0] = result
        return success(result)
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Either<ApiError, AuthenticatedUser> {
        val user =
            users.find { it.username == username }
                ?: return Either.Left(ApiError("User not found"))
        if (passwords[user.id] != password) {
            return Either.Left(ApiError("Invalid password"))
        }
        val token = token[user.id] ?: return Either.Left(ApiError("Token not found"))
        return success(AuthenticatedUser(user, token))
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

    override suspend fun findUserById(id: Int): Either<ApiError, User> {
        val user = users.find { it.id == id } ?: return Either.Left(ApiError("User not found"))
        return success(user)
    }

    override suspend fun logout(): Either<ApiError, Unit> {
        token.clear()
        return success(Unit)
    }

    override suspend fun findUserByUsername(query: String): Either<ApiError, List<User>> {
        val result = users.filter { it.username.contains(query, ignoreCase = true) }
        return success(result)
    }
}
