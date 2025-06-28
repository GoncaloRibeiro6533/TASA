package pt.isel.session

import kotlinx.datetime.Instant
import pt.isel.Session
import pt.isel.TokenValidationInfo
import pt.isel.User

/**
 * Interface that defines the operations that can be done on the Session repository.
 */
interface SessionRepository {
    fun findByToken(tokenValidationInfo: TokenValidationInfo): Session?

    fun findByUser(user: User): List<Session>

    fun createSession(
        user: User,
        accessTokenValidationInfo: TokenValidationInfo,
        accessCreatedAt: Instant,
        accessLastUsedAt: Instant,
        accessExpiresAt: Instant,
        maxTokens: Int,
        refreshTokenValidationInfo: TokenValidationInfo,
        refreshCreatedAt: Instant,
        refreshExpiresAt: Instant,
    ): Session

    fun getSessionHistory(
        user: User,
        limit: Int,
        skip: Int,
    ): List<Session>

    fun deleteSession(session: Session): Boolean

    fun clear()

    fun updateSession(
        session: Session,
        lastTimeUsed: Instant,
    ): Session
}
