package pt.isel.mappers

import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.RefreshToken
import pt.isel.TokenValidationInfo
import java.sql.ResultSet

class RefreshTokenMapper : ColumnMapper<RefreshToken> {
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): RefreshToken? {
        return RefreshToken(
            id = rs.getInt("refresh_token_id"),
            tokenValidationInfo =
                TokenValidationInfo(
                    validationInfo = rs.getString("refresh_token"),
                ),
            createdAt = Instant.fromEpochSeconds(rs.getLong("refresh_created_at")),
            expiresAt =
                Instant.fromEpochSeconds(
                    rs.getLong("refresh_expire_at"),
                ),
        )
    }
}
