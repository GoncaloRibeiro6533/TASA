package com.tasa.service.interfaces

import com.tasa.domain.ApiError
import com.tasa.domain.user.User
import com.tasa.service.http.models.user.LoginOutput
import com.tasa.utils.Either

/**
 * UserService defines the contract for managing user-related operations in the application.
 * It provides methods for user authentication, registration, and session management.
 */
interface UserService {
    /**
     * Logs in a user with the specified username and password.
     * @param username The username of the user.
     * @param password The password of the user.
     * @return Either an ApiError or the LoginOutput containing session information.
     */
    suspend fun login(
        username: String,
        password: String,
    ): Either<ApiError, LoginOutput>

    /**
     * Registers a new user with the specified username, password, and email.
     * @param username The username of the user.
     * @param password The password of the user.
     * @param email The email of the user.
     * @return Either an ApiError or the created User.
     */
    suspend fun register(
        username: String,
        password: String,
        email: String,
    ): Either<ApiError, User>

    /**
     * Fetches a user by their ID.
     * @param id The ID of the user to be retrieved.
     * @param token The authentication token for the request.
     * @return Either an ApiError or the retrieved User.
     */
    suspend fun findUserById(
        id: Int,
        token: String,
    ): Either<ApiError, User>

    /**
     * Logs out the current user.
     * @param token The authentication token for the request.
     * @return Either an ApiError or Unit if successful.
     */
    suspend fun logout(token: String): Either<ApiError, Unit>

    /**
     * Refreshes the user session and retrieves a new session token.
     * @param token The current authentication token.
     * @param refreshToken The refresh token used to obtain a new session token.
     * @return Either an ApiError or the new LoginOutput containing session information.
     */
    suspend fun refreshToken(
        token: String,
        refreshToken: String,
    ): Either<ApiError, LoginOutput>
}
