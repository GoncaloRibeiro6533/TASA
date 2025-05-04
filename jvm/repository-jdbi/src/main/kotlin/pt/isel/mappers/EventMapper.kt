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
            id = rs.getLong("event_id"),
            calendarId = rs.getLong("calendar_id"),
            title = rs.getString("title"),
        )
    }
}
