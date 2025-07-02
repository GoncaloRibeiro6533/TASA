package com.tasa.service.http

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.service.http.models.user.LoginOutput
import com.tasa.service.http.models.user.UserLoginCredentialsInput
import com.tasa.service.http.models.user.UserRegisterInput
import com.tasa.service.http.utils.get
import com.tasa.service.http.utils.post
import com.tasa.service.interfaces.UserService
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.failure
import com.tasa.utils.success
import io.ktor.client.HttpClient

class UserServiceHttp(private val client: HttpClient) : UserService {
    override suspend fun login(
        username: String,
        password: String,
    ): Either<ApiError, LoginOutput> {
        return when (
            val response =
                client.post<LoginOutput>(
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
                client.post<User>(
                    url = "/user/pdm/register",
                    body = UserRegisterInput(username, email, password),
                )
        ) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }

    override suspend fun findUserById(
        id: Int,
        token: String,
    ): Either<ApiError, User> {
        return when (val response = client.get<User>("/user/$id", token = token)) {
            is Success -> success(response.value)
            is Failure -> failure(response.value)
        }
    }

    override suspend fun logout(token: String): Either<ApiError, Unit> {
        return when (
            val response =
                client.post<Unit>(
                    url = "/user/logout",
                    token = token,
                )
        ) {
            is Success -> success(Unit)
            is Failure -> failure(response.value)
        }
    }
}
