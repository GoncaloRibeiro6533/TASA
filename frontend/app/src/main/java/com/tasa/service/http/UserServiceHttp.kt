package com.tasa.service.http

import com.tasa.domain.ApiError
import com.tasa.domain.user.AuthenticatedUser
import com.tasa.domain.user.User
import com.tasa.service.UserService
import com.tasa.service.http.models.user.UserDTO
import com.tasa.service.http.models.user.UserLoginCredentialsInput
import com.tasa.service.http.models.user.UserRegisterInput
import com.tasa.service.http.utils.get
import com.tasa.service.http.utils.post
import com.tasa.service.http.utils.put
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import io.ktor.client.HttpClient

class UserServiceHttp(private val client: HttpClient) : UserService {
    override suspend fun fetchUser(): Either<ApiError, User> {
        TODO()
    }

    override suspend fun updateUsername(newUsername: String): Either<ApiError, User> {
        return when (
            val response =
                client.put<UserDTO>(
                    url = "/user/edit/username",
                    body = null, // UsernameUpdateInput(newUsername)
                )
        ) {
            is Success -> success(response.value.toUser())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun login(
        username: String,
        password: String,
    ): Either<ApiError, AuthenticatedUser> {
        return when (
            val response =
                client.post<AuthenticatedUser>(
                    url = "/user/login",
                    body = UserLoginCredentialsInput(username = username, password = password),
                )
        ) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        email: String,
    ): Either<ApiError, User> =
        when (
            val response =
                client.post<UserDTO>(
                    url = "/user/pdm/register",
                    body = UserRegisterInput(username, email, password),
                )
        ) {
            is Success -> success(response.value.toUser())
            is Failure -> failure(response.value)
        }

    override suspend fun findUserById(id: Int): Either<ApiError, User> {
        return when (val response = client.get<UserDTO>("/user/$id")) {
            is Success -> success(response.value.toUser())
            is Failure -> failure(response.value)
        }
    }

    override suspend fun logout(): Either<ApiError, Unit> {
        return when (
            val response =
                client.post<Unit>(
                    url = "/user/logout",
                )
        ) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun findUserByUsername(query: String): Either<ApiError, List<User>> {
        TODO()
    }
}
