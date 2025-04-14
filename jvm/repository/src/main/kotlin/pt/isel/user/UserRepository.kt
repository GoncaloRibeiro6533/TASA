package pt.isel.user

import pt.isel.User

/**
 * Interface that defines the operations that can be done on the User repository.
 */
interface UserRepository {
    fun findById(id: Int): User?

    fun findByUsername(
        username: String,
        limit: Int,
        skip: Int,
    ): List<User>

    fun createUser(
        username: String,
        email: String,
        password: String,
    ): User

    fun updateUsername(
        user: User,
        newUsername: String,
    ): User

    fun findByUsernameAndPassword(
        username: String,
        password: String,
    ): User?

    fun delete(user: User)

    @Suppress("RedundantUnitReturnType")
    fun clear(): Unit

    fun findAll(): List<User>

    fun findByEmail(email: String): User?

    fun findPasswordOfUser(user: User): String

    fun findUserMatchesUsername(username: String): User?
}
