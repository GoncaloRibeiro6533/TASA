package com.tasa.repository

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.repository.interfaces.UserRepositoryInterface
import com.tasa.service.TasaService
import com.tasa.storage.TasaDB
import com.tasa.storage.entities.UserEntity
import com.tasa.utils.Either
import com.tasa.utils.Failure
import com.tasa.utils.Success
import com.tasa.utils.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val local: TasaDB,
    private val remote: TasaService,
) : UserRepositoryInterface {
    override suspend fun insertUser(users: List<User>) {
        local.userDao().insertUsers(
            *users.map {
                UserEntity(
                    it.id,
                    it.username,
                    it.email,
                )
            }.toTypedArray(),
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

    override suspend fun updateUser(user: User) {
        local.userDao().updateUsername(user.id, user.username)
    }

    override suspend fun changeUsername(newUsername: String): Either<ApiError, User> {
        val result = remote.userService.updateUsername(newUsername)
        when (result) {
            is Success -> {
                updateUser(result.value)
                return success(result.value)
            }
            is Failure -> return result
        }
    }

    override suspend fun fetchByUsername(username: String) = remote.userService.findUserByUsername(username)

    override suspend fun clear() {
        local.userDao().clear()
    }
}
