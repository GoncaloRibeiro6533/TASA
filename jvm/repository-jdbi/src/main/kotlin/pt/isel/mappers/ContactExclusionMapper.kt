package pt.isel.mappers

import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import pt.isel.ContactExclusion
import java.sql.ResultSet

class ContactExclusionMapper : RowMapper<ContactExclusion> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): ContactExclusion {
        return ContactExclusion(
            id = rs.getInt("id"),
            name = rs.getString("name"),
            phoneNumber = rs.getString("phone_number"),
        )
    }
}
