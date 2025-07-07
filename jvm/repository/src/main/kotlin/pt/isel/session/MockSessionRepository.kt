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
    private var tokenId = 0
    private var refreshTokenId = 0
    private val tokens = mutableMapOf<Int, Token>()
    private val refreshTokens = mutableMapOf<Int, RefreshToken>()

    override fun findByToken(tokenValidationInfo: TokenValidationInfo): Session? {
        return sessions.values.flatten().find { session ->
            session.token.tokenValidationInfo.validationInfo == tokenValidationInfo.validationInfo
        }
    }

    override fun findByTokens(
        tokenValidationInfo: TokenValidationInfo,
        refreshTokenValidationInfo: TokenValidationInfo,
    ): Session? {
        return sessions.values.flatten().find { session ->
            session.token.tokenValidationInfo.validationInfo == tokenValidationInfo.validationInfo &&
                session.refreshToken.tokenValidationInfo.validationInfo ==
                refreshTokenValidationInfo.validationInfo
        }
    }

    override fun findByUser(user: User): List<Session> {
        return sessions[user.id] ?: emptyList()
    }

    override fun createSession(
        user: User,
        accessTokenValidationInfo: TokenValidationInfo,
        accessCreatedAt: Instant,
        accessLastUsedAt: Instant,
        accessExpiresAt: Instant,
        maxTokens: Int,
        refreshTokenValidationInfo: TokenValidationInfo,
        refreshCreatedAt: Instant,
        refreshExpiresAt: Instant,
    ): Session {
        val sessionId = sessionId++
        val tokenId = tokenId++
        val refreshTokenId = refreshTokenId++
        val token =
            Token(
                id = tokenId,
                tokenValidationInfo = accessTokenValidationInfo,
                createdAt = accessCreatedAt,
                lastUsedAt = accessLastUsedAt,
                expiresAt = accessExpiresAt,
            )
        tokens[sessionId] = token
        val refreshToken =
            RefreshToken(
                id = refreshTokenId,
                tokenValidationInfo = refreshTokenValidationInfo,
                createdAt = refreshCreatedAt,
                expiresAt = refreshExpiresAt,
            )
        refreshTokens[sessionId] = refreshToken
        val session =
            Session(
                id = sessionId,
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
