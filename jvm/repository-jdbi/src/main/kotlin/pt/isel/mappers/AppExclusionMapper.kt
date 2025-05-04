package pt.isel.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.AppExclusion
import java.sql.ResultSet

class AppExclusionMapper : RowMapper<AppExclusion> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): AppExclusion {
        return AppExclusion(
            id = rs.getInt("id"),
            name = rs.getString("app_name"),
        )
    }
}
