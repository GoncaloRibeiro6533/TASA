package pt.isel.mappers

import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.Token
import pt.isel.TokenValidationInfo
import java.sql.ResultSet

class TokenMapper : ColumnMapper<Token> {
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Token {
        return Token(
            id = rs.getInt("access_token_id"),
            tokenValidationInfo = TokenValidationInfo(rs.getString("access_token")),
            createdAt = Instant.fromEpochSeconds(rs.getLong("access_created_at")),
            lastUsedAt = Instant.fromEpochSeconds(rs.getLong("access_last_used_at")),
            expiresAt = Instant.fromEpochSeconds(rs.getLong("access_expires_at")),
        )
    }
}
