package com.tasa.repository

import com.tasa.domain.ApiError
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
) : UserRepositoryInterface {
    override suspend fun createUser(
        username: String,
        email: String,
        password: String,
    ): Either<ApiError, User> {
        val result = remote.userService.register(username, email, password)
        when (result) {
            is Success -> {
                val userEntity = result.value.toUserEntity()
                local.userDao().insertUser(userEntity)
                return success(result.value)
            }
            is Failure -> return failure(result.value)
        }
    }

    override suspend fun insertUser(user: User) {
        // remote.userService.register()
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
}
