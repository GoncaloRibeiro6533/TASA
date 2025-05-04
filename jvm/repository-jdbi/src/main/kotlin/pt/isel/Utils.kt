package pt.isel

import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.PostgresPlugin
import pt.isel.mappers.AppExclusionMapper
import pt.isel.mappers.ContactExclusionMapper
import pt.isel.mappers.EventMapper
import pt.isel.mappers.InstantMapper
import pt.isel.mappers.LocationMapper
import pt.isel.mappers.RefreshTokenMapper
import pt.isel.mappers.RuleEventMapper
import pt.isel.mappers.RuleLocationMapper
import pt.isel.mappers.SessionMapper
import pt.isel.mappers.TokenMapper
import pt.isel.mappers.TokenValidationInfoMapper
import pt.isel.mappers.UserMapper
import java.time.Instant

fun Jdbi.configureWithAppRequirements(): Jdbi {
    installPlugin(KotlinPlugin())
    installPlugin(PostgresPlugin())
    registerColumnMapper(User::class.java, UserMapper())
    registerColumnMapper(TokenValidationInfo::class.java, TokenValidationInfoMapper())
    registerColumnMapper(Instant::class.java, InstantMapper())
    registerRowMapper(AppExclusion::class.java, AppExclusionMapper())
    registerRowMapper(ContactExclusion::class.java, ContactExclusionMapper())
    registerColumnMapper(Location::class.java, LocationMapper())
    registerRowMapper(Event::class.java, EventMapper())
    registerRowMapper(RuleLocation::class.java, RuleLocationMapper())
    registerRowMapper(RuleEvent::class.java, RuleEventMapper())
    registerColumnMapper(RefreshToken::class.java, RefreshTokenMapper())
    registerRowMapper(Session::class.java, SessionMapper())
    registerColumnMapper(Token::class.java, TokenMapper())
    // registerRowMapper(Session::class.java, SessionRowMapper())

    return this
}
