package pt.isel

/**
 * Represents an authenticated user with their associated token information.
 *
 * @property user the user information.
 * @property token the session token.
 */
data class AuthenticatedUser(
    val user: User,
    val token: String,
)
