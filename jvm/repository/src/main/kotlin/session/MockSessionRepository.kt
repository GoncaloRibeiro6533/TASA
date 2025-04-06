package session

import kotlinx.datetime.Instant
import pt.isel.Session
import pt.isel.TokenValidationInfo
import pt.isel.User

class MockSessionRepository : SessionRepository {
    private val sessions = mutableMapOf<Int, MutableList<Session>>()

    override fun findByToken(token: TokenValidationInfo): Session? {
        return sessions.values.flatten().find {
            it.token == token
        }
    }

    override fun findByUser(user: User): List<Session> {
        return sessions[user.id] ?: emptyList()
    }

    override fun createSession(
        user: User,
        token: TokenValidationInfo,
        refreshToken: TokenValidationInfo,
        createdAt: Instant,
        lastTimeUsed: Instant,
        expirationDate: Instant,
    ): Session {
        val session =
            Session(
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
                createdAt = createdAt,
                lastUsedAt = lastTimeUsed,
                expirationDate = expirationDate,
            )
        sessions.computeIfAbsent(user.id) { mutableListOf() }
            .add(session)
        return session
    }

    override fun getSessionHistory(
        user: User,
        limit: Int,
        skip: Int,
    ): List<Session> {
        return sessions[user.id]?.drop(skip)?.take(limit) ?: emptyList()
    }

    override fun deleteSession(session: Session): Boolean {
        sessions[session.userId]?.remove(session)?.let {
            return it
        } ?: return false
    }

    override fun clear() {
        sessions.clear()
    }

    override fun updateSession(
        session: Session,
        lastTimeUsed: Instant,
    ): Session {
        val updatedSession = session.copy(lastUsedAt = lastTimeUsed)
        sessions[session.userId]?.remove(session)
        sessions[session.userId]?.add(updatedSession)
        return updatedSession
    }
}
