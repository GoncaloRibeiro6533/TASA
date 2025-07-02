package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow

interface UserRepositoryInterface {
    suspend fun createUser(
        username: String,
        email: String,
        password: String,
    ): Either<ApiError, User>

    fun getUsers(): Flow<List<User>>

    suspend fun insertUser(user: User)

    suspend fun clear()
}
