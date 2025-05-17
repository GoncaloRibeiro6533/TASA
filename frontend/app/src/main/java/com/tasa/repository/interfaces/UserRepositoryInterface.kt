package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.utils.Either
import kotlinx.coroutines.flow.Flow

interface UserRepositoryInterface {
    suspend fun changeUsername(newUsername: String): Either<ApiError, User>

    fun getUsers(): Flow<List<User>>

    suspend fun insertUser(users: List<User>)

    suspend fun updateUser(user: User)

    suspend fun clear()

    suspend fun fetchByUsername(username: String): Either<ApiError, List<User>>
}
