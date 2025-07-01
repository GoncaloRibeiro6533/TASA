package pt.isel.mappers

import kotlinx.datetime.toKotlinLocalDateTime
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.Event
import pt.isel.RuleEvent
import pt.isel.User
import java.sql.ResultSet

class RuleEventMapper : RowMapper<RuleEvent> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): RuleEvent {
        return RuleEvent(
            id = rs.getInt("id"),
            startTime = rs.getTimestamp("start_time").toLocalDateTime().toKotlinLocalDateTime(),
            endTime = rs.getTimestamp("end_time").toLocalDateTime().toKotlinLocalDateTime(),
            creator =
                User(
                    id = rs.getInt("user_id"),
                    username = rs.getString("username"),
                    email = rs.getString("email"),
                ),
            event =
                Event(
                    id = rs.getInt("event_id"),
                    title = rs.getString("title"),
                    startTime = rs.getTimestamp("event_start_time").toLocalDateTime().toKotlinLocalDateTime(),
                    endTime = rs.getTimestamp("event_end_time").toLocalDateTime().toKotlinLocalDateTime(),
                ),
        )
    }
}
