package pt.isel.session

import kotlinx.datetime.Instant
import pt.isel.RefreshToken
import pt.isel.Session
import pt.isel.Token
import pt.isel.TokenValidationInfo
import pt.isel.User

class MockSessionRepository : SessionRepository {
    private val sessions = mutableMapOf<Int, MutableList<Session>>()
    private var sessionId = 0
    private val tokens = mutableMapOf<Int, Token>()
    private val refreshTokens = mutableMapOf<Int, RefreshToken>()

    override fun findByToken(token: TokenValidationInfo): Session? {
        return sessions.values.flatten().find {
            it.token.tokenValidationInfo == token
        }
    }

    override fun findByUser(user: User): List<Session> {
        return sessions[user.id] ?: emptyList()
    }

    override fun createSession(
        user: User,
        token: Token,
        refreshToken: RefreshToken,
        maxTokens: Int,
    ): Session {
        val session =
            Session(
                id = sessionId++,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
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
        val updatedToken = session.token.copy(lastUsedAt = lastTimeUsed)
        val updatedSession =
            session.copy(
                token = updatedToken,
            )
        sessions[session.userId]?.remove(session)
        sessions[session.userId]?.add(updatedSession)
        return updatedSession
    }
}
