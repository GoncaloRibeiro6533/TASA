package pt.isel

import kotlinx.datetime.Instant

/**
 * Represents a session defined by its token, refreshToken, userId, createdAt, lastUsedAt and expirationDate.
 * @property token the session's token
 * @property refreshToken the session's refreshToken
 * @property userId the id of the user that owns the session
 * @throws IllegalArgumentException if any of the parameters is invalid
 */
data class Session(
    val id: Int,
    val token: Token,
    val refreshToken: RefreshToken,
    val userId: Int,
) {
    init {
        require(userId >= 0) { "userId must be positive" }
        // require(expirationDate > createdAt) { "expirationDate must be after createdAt" }
        // require(lastUsedAt >= createdAt) { "lastUsedAt must be after createdAt" }
    }
}

data class Token(
    val id: Int,
    val tokenValidationInfo: TokenValidationInfo,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val expiresAt: Instant,
)

data class RefreshToken(
    val id: Int,
    val tokenValidationInfo: TokenValidationInfo,
    val createdAt: Instant,
    val expiresAt: Instant,
)
