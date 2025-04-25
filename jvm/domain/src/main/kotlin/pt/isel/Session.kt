package pt.isel

import kotlinx.datetime.Instant

/**
 * Represents a session defined by its token, refreshToken, userId, createdAt, lastUsedAt and expirationDate.
 * @property token the session's token
 * @property refreshToken the session's refreshToken
 * @property userId the id of the user that owns the session
 * @property createdAt the date and time when the session was created
 * @property lastUsedAt the date and time when the session was last used
 * @property expirationDate the date and time when the session expires
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
data class Session(
    val token: TokenValidationInfo,
    val refreshToken: TokenValidationInfo,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val expirationDate: Instant,
) {
    init {
        require(userId >= 0) { "userId must be positive" }
        require(expirationDate > createdAt) { "expirationDate must be after createdAt" }
        require(lastUsedAt >= createdAt) { "lastUsedAt must be after createdAt" }
    }
}

data class Token(
    val tokenValidationInfo: TokenValidationInfo,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)
