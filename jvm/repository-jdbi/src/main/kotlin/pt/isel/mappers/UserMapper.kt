package pt.isel.mappers

import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.User
import java.sql.ResultSet

class UserMapper : ColumnMapper<User> {
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): User {
        return User(
            id = rs.getInt("id"),
            username = rs.getString("username"),
            email = rs.getString("email"),
        )
    }
}
