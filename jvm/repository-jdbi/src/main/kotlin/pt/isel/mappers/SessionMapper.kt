package pt.isel.mappers

import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.RefreshToken
import pt.isel.Session
import pt.isel.Token
import pt.isel.TokenValidationInfo
import java.sql.ResultSet

class SessionMapper : RowMapper<Session> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
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
