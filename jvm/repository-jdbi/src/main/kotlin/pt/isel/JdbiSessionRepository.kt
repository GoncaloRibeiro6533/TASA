package pt.isel

import kotlinx.datetime.Instant
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.mapper.RowMapper
import org.slf4j.LoggerFactory
import pt.isel.session.SessionRepository

class JdbiSessionRepository(
    private val handle: Handle,
) : SessionRepository {
    override fun findByToken(token: TokenValidationInfo): Session? {
        logger.info("findByToken: $token")
        return handle.createQuery(
            """
            SELECT
                   s.id AS session_id,
                   s.user_id,
                   a.token AS access_token,
                   a.created_at AS access_created_at,
                   a.last_used_at AS access_last_used_at,
                   a.expire_at AS access_expire_at,
                   r.token AS refresh_token,
                   r.created_at AS refresh_created_at,
                   r.expire_at AS refresh_expire_at
               FROM ps.ACCESS_TOKEN a
               JOIN ps.SESSION s ON a.session_id = s.id
               LEFT JOIN ps.REFRESH_TOKEN r ON s.id = r.session_id
               WHERE a.token = :token_value
            """.trimIndent(),
        )
            .bind("token_value", token.validationInfo)
            .mapTo(Session::class.java)
            // .map<Session>(SessionMapper())
            .findOne()
            .orElse(null)
    }

    override fun findByUser(user: User): List<Session> {
        return handle.createQuery(
            """
            SELECT
                s.id AS session_id,
                s.user_id,
                a.token AS access_token,
                a.created_at AS access_created_at,
                a.last_used_at AS access_last_used_at,
                a.expire_at AS access_expire_at,
                r.token AS refresh_token,
                r.created_at AS refresh_created_at,
                r.expire_at AS refresh_expire_at
            FROM ps.SESSION s
            JOIN ps.ACCESS_TOKEN a ON s.id = a.session_id
            LEFT JOIN ps.REFRESH_TOKEN r ON s.id = r.session_id
            WHERE s.user_id = :user_id;
            """.trimIndent(),
        )
            .bind("user_id", user.id)
            .mapTo(Session::class.java)
            .list()
    }

    override fun createSession(
        user: User,
        token: Token,
        refreshToken: RefreshToken,
        maxTokens: Int,
    ): Session {
        val deletions =
            handle.createUpdate(
                """
                Delete from ps.SESSION
                where user_id = :user_id AND id in (
                    select id from ps.ACCESS_TOKEN where user_id = :user_id order by last_used_at desc offset :offset
                )
                """.trimIndent(),
            )
                .bind("user_id", user.id)
                .bind("offset", maxTokens - 1)
                .execute()
        val session =
            handle.createUpdate(
                """
                INSERT INTO ps.SESSION (user_id)
                VALUES (:user_id)
                RETURNING id
                """.trimIndent(),
            )
                .bind("user_id", user.id)
                .executeAndReturnGeneratedKeys().mapTo(Int::class.java).one()
        handle.createUpdate(
            """
            INSERT INTO ps.ACCESS_TOKEN (token, session_id, created_at, last_used_at, expire_at)
            VALUES (:token, :session_id, :created_at, :last_used_at, :expire_at)
            """.trimIndent(),
        )
            .bind("token", token.tokenValidationInfo.validationInfo)
            .bind("session_id", session)
            .bind("created_at", token.createdAt.toEpochMilliseconds())
            .bind("last_used_at", token.lastUsedAt.toEpochMilliseconds())
            .bind("expire_at", token.expiresAt.toEpochMilliseconds())
            .execute()
        handle.createUpdate(
            """
            INSERT INTO ps.REFRESH_TOKEN (token, session_id, created_at, expire_at)
            VALUES (:token, :session_id, :created_at, :expire_at)
            """.trimIndent(),
        )
            .bind("token", refreshToken.tokenValidationInfo.validationInfo)
            .bind("session_id", session)
            .bind("created_at", refreshToken.createdAt.toEpochMilliseconds())
            .bind("expire_at", refreshToken.expiresAt.toEpochMilliseconds())
            .execute()
        return Session(
            id = session,
            userId = user.id,
            token = token,
            refreshToken = refreshToken,
        )
    }

    override fun getSessionHistory(
        user: User,
        limit: Int,
        skip: Int,
    ): List<Session> {
        TODO("Not yet implemented")
    }

    override fun deleteSession(session: Session): Boolean {
        return handle.createUpdate(
            """
            DELETE FROM ps.SESSION WHERE id = :id
            """.trimIndent(),
        )
            .bind("id", session.id)
            .execute() > 0
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM ps.SESSION")
            .execute()
    }

    override fun updateSession(
        session: Session,
        lastTimeUsed: Instant,
    ): Session {
        handle.createUpdate(
            """
            UPDATE ps.ACCESS_TOKEN SET last_used_at = :last_used_at
            WHERE session_id = :session_id
            """.trimIndent(),
        )
            .bind("session_id", session.id)
            .bind("last_used_at", lastTimeUsed.toEpochMilliseconds())
            .execute()
        return session.copy(token = session.token.copy(lastUsedAt = lastTimeUsed))
    }

    private class SessionMapper : RowMapper<Session> {
        override fun map(
            rs: java.sql.ResultSet,
            ctx: org.jdbi.v3.core.statement.StatementContext,
        ): Session {
            return Session(
                id = rs.getInt("session_id"),
                userId = rs.getInt("user_id"),
                token =
                    Token(
                        tokenValidationInfo =
                            TokenValidationInfo(
                                validationInfo = rs.getString("access_token"),
                            ),
                        createdAt = Instant.fromEpochMilliseconds(rs.getLong("access_created_at")),
                        lastUsedAt = Instant.fromEpochMilliseconds(rs.getLong("access_last_used_at")),
                        expiresAt = Instant.fromEpochMilliseconds(rs.getLong("access_expire_at")),
                    ),
                refreshToken =
                    RefreshToken(
                        tokenValidationInfo =
                            TokenValidationInfo(
                                validationInfo = rs.getString("refresh_token"),
                            ),
                        createdAt = Instant.fromEpochMilliseconds(rs.getLong("refresh_created_at")),
                        expiresAt =
                            Instant.fromEpochSeconds(
                                rs.getLong("refresh_expire_at"),
                            ),
                    ),
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SessionMapper::class.java)
    }
}
