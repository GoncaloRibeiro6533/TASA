import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.JdbiEventRepository
import pt.isel.JdbiLocationRepository
import pt.isel.JdbiRuleRepository
import pt.isel.JdbiSessionRepository
import pt.isel.JdbiUserRepository
import pt.isel.configureWithAppRequirements
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.days

class JdbiRulesRepositoryTests {
    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        private val testClock = TestClock()
    }

    @BeforeEach
    fun clearDatabase() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).clear()
            JdbiSessionRepository(handle).clear()
            JdbiRuleRepository(handle).clear()
            JdbiEventRepository(handle).clear()
            JdbiLocationRepository(handle).clear()
        }
    }

    @Test
    fun `should create a rule location`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            val rule =
                ruleRepo.createLocationRule(
                    location = location,
                    user = user,
                )
            assertEquals(location.id, rule.location.id)
            assertEquals(user.id, rule.creator.id)
        }
    }

    @Test
    fun `should create rule event`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val eventRepo = JdbiEventRepository(handle)
            val event =
                eventRepo.create(
                    "Test Event",
                    user,
                    "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val startTime = testClock.now().plus(2.days).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            val endTime = testClock.now().plus(3.days).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            val rule =
                ruleRepo.createEventRule(
                    event = event,
                    user = user,
                    startTime = startTime,
                    endTime = endTime,
                )
            assertEquals(event.id, rule.event.id)
            assertEquals(user.id, rule.creator.id)
            assertEquals(startTime, rule.startTime)
            assertEquals(endTime, rule.endTime)
        }
    }

    @Test
    fun `should find rules by user`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            ruleRepo.createLocationRule(
                location = location,
                user = user,
            )
            val rules = ruleRepo.findByUserId(user)
            assertEquals(1, rules.size)
            assertEquals(user.id, rules[0].creator.id)
        }
    }

    @Test
    fun `should update an event rule`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val eventRepo = JdbiEventRepository(handle)
            val event =
                eventRepo.create(
                    "Test Event",
                    user,
                    "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val startTime = testClock.now().plus(2.days).toLocalDateTime(TimeZone.currentSystemDefault())
            val endTime = testClock.now().plus(3.days).toLocalDateTime(TimeZone.currentSystemDefault())
            val rule =
                ruleRepo.createEventRule(
                    event = event,
                    user = user,
                    startTime = startTime.toJavaLocalDateTime(),
                    endTime = endTime.toJavaLocalDateTime(),
                )
            val newStartTime = testClock.now().plus(4.days).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            val newEndTime = testClock.now().plus(5.days).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            val updatedRule = ruleRepo.updateRuleEvent(rule, newStartTime, newEndTime)
            assertEquals(rule.id, updatedRule.id)
            assertEquals(event.id, updatedRule.event.id)
            assertEquals(user.id, updatedRule.creator.id)
            assertEquals(newStartTime, updatedRule.startTime)
            assertEquals(newEndTime, updatedRule.endTime)
        }
    }

    @Test
    fun `should delete a rule by ID`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            val rule =
                ruleRepo.createLocationRule(
                    location,
                    user,
                )
            val deleted =
                ruleRepo.deleteLocationEvent(
                    rule,
                )
            assertEquals(true, deleted)
            val rulesAfterDeletion = ruleRepo.findByUserId(user)
            assertEquals(0, rulesAfterDeletion.size)
        }
    }

    @Test
    fun `should delete a rule by ID for event rules`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val eventRepo = JdbiEventRepository(handle)
            val event =
                eventRepo.create(
                    "Test Event",
                    user,
                    "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val startTime = testClock.now().plus(2.days).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            val endTime = testClock.now().plus(3.days).toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()
            val rule =
                ruleRepo.createEventRule(
                    event = event,
                    user = user,
                    startTime = startTime,
                    endTime = endTime,
                )
            val deleted =
                ruleRepo.deleteRuleEvent(
                    rule,
                )
            assertEquals(true, deleted)
            val rulesAfterDeletion = ruleRepo.findByUserId(user)
            assertEquals(0, rulesAfterDeletion.size)
        }
    }

    @Test
    fun `should find all rules`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            val rule =
                ruleRepo.createLocationRule(
                    location,
                    user,
                )
            val rules = ruleRepo.findAll()
            assertEquals(1, rules.size)
            assertEquals(rule.id, rules[0].id)
            assertEquals(user.id, rules[0].creator.id)
        }
    }

    @Test
    fun `should clear all rules`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val ruleRepo = JdbiRuleRepository(handle)
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            ruleRepo.createLocationRule(
                location,
                user,
            )
            ruleRepo.clear()
            val rules = ruleRepo.findAll()
            assertEquals(0, rules.size)
        }
    }
}
