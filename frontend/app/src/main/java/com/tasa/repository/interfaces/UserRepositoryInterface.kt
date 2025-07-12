package com.tasa.repository.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.utils.Either

/**
 * UserRepositoryInterface defines the contract for managing users in the application.
 * It provides methods to create users, manage user sessions, and retrieve user information.
 */
interface UserRepositoryInterface {
    /**
     * Creates a new user with the specified parameters.
     * @param username The username of the user.
     * @param email The email of the user.
     * @param password The password of the user.
     * @return Either an ApiError or the created User.
     */
    suspend fun createUser(
        username: String,
        email: String,
        password: String,
    ): Either<ApiError, User>

    /**
     * Clears all user data from the repository.
     */
    suspend fun clear()

    /**
     * Creates a user session token with the specified email and password.
     * @param email The email of the user.
     * @param password The password of the user.
     * @return Either an ApiError or the created User.
     */
    suspend fun createToken(
        email: String,
        password: String,
    ): Either<ApiError, User>

    /**
     * Logouts the current user.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun logout(): Either<ApiError, Unit>

    /**
     * Refreshes the user session, and retrieves a new session token.
     * @return Either an ApiError or the new session token.
     */
    suspend fun refreshSession(): Either<ApiError, String>
}
