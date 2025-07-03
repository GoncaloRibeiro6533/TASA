package pt.isel.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.Event
import java.sql.ResultSet

class EventMapper : RowMapper<Event> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): Event {
        return Event(
            id = rs.getInt("id"),
            title = rs.getString("title"),
            startTime = rs.getTimestamp("start_time").toLocalDateTime(),
            endTime = rs.getTimestamp("end_time").toLocalDateTime(),
        )
    }
}
