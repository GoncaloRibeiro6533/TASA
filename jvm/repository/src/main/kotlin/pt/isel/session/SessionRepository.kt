package pt.isel.session

import kotlinx.datetime.Instant
import pt.isel.Session
import pt.isel.TokenValidationInfo
import pt.isel.User

/**
 * Interface that defines the operations that can be done on the Session repository.
 */
interface SessionRepository {
    fun findByToken(token: TokenValidationInfo): Session?

    fun findByUser(user: User): List<Session>

    fun createSession(
        user: User,
        token: TokenValidationInfo,
        refreshToken: TokenValidationInfo,
        createdAt: Instant,
        lastTimeUsed: Instant,
        expirationDate: Instant,
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
