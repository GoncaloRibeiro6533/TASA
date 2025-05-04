package pt.isel.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.Location
import java.sql.ResultSet

class LocationMapper : ColumnMapper<Location> {
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Location {
        return Location(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            latitude = rs.getDouble("latitude"),
            longitude = rs.getDouble("longitude"),
            radius = rs.getDouble("radius"),
        )
    }
}
