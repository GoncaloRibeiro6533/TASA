package pt.isel.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.Location
import pt.isel.RuleLocation
import pt.isel.User
import java.sql.ResultSet

class RuleLocationMapper : RowMapper<RuleLocation> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): RuleLocation {
        return RuleLocation(
            id = rs.getInt("id"),
            creator =
                User(
                    id = rs.getInt("user_id"),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                ),
            location =
                Location(
                    id = rs.getInt("location_id"),
                    name = rs.getString("name"),
                    latitude = rs.getDouble("latitude"),
                    longitude = rs.getDouble("longitude"),
                    radius = rs.getDouble("radius"),
                ),
        )
    }
}
