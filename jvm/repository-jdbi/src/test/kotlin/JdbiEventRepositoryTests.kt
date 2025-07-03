import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.JdbiEventRepository
import pt.isel.JdbiLocationRepository
import pt.isel.JdbiRuleRepository
import pt.isel.JdbiSessionRepository
import pt.isel.JdbiUserRepository
import pt.isel.configureWithAppRequirements
import kotlin.test.Test
import kotlin.test.assertNotNull

class JdbiEventRepositoryTests {
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
    fun `should create an event`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    title = "Test Event",
                    user = user,
                    startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val createdEvent = eventRepo.findById(event.id)
            assertNotNull(createdEvent)
            assertEquals(event.id, createdEvent.id)
            assertEquals(event.title, createdEvent.title)
            assertEquals(event.startTime, createdEvent.startTime)
            assertEquals(event.endTime, createdEvent.endTime)
        }
    }

    @Test
    fun `should find an event by id`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    title = "Test Event",
                    user = user,
                    startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val foundEvent = eventRepo.findById(event.id)
            assertNotNull(foundEvent)
            assertEquals(event.id, foundEvent.id)
            assertEquals(event.title, foundEvent.title)
            assertEquals(event.startTime, foundEvent.startTime)
            assertEquals(event.endTime, foundEvent.endTime)
        }
    }

    @Test
    fun `should update an event`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    title = "Test Event",
                    user = user,
                    startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val updatedEvent =
                eventRepo.update(
                    user,
                    event,
                    newTitle = "Updated Event Title",
                )
            assertNotNull(updatedEvent)
            assertEquals("Updated Event Title", updatedEvent.title)
            val sut = eventRepo.findById(event.id)
            assertNotNull(sut)
            assertEquals("Updated Event Title", sut.title)
            assertEquals(event.startTime, sut.startTime)
            assertEquals(event.endTime, sut.endTime)
            assertEquals(event.id, sut.id)
        }
    }

    @Test
    fun `should delete an event`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    title = "Test Event",
                    user = user,
                    startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val deleted = eventRepo.delete(event = event, user = user)
            assertEquals(true, deleted)
            val foundEvent = eventRepo.findById(event.id)
            assertEquals(null, foundEvent)
        }
    }

    @Test
    fun `should find all events for a user`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            eventRepo.create(
                title = "Event 1",
                user = user,
                startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
            )
            eventRepo.create(
                title = "Event 2",
                user = user,
                startTime = "2025-10-01T12:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-10-01T13:00".toLocalDateTime().toJavaLocalDateTime(),
            )
            val events = eventRepo.findByUserId(user)
            assertEquals(2, events.size)
            assertTrue(events.any { it.title == "Event 1" })
            assertTrue(events.any { it.title == "Event 2" })
        }
    }

    @Test
    fun `should delete event`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val eventRepo = JdbiEventRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    title = "Test Event",
                    user = user,
                    startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                    endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
                )
            val deleted = eventRepo.delete(event = event, user = user)
            assertEquals(true, deleted)
            val foundEvent = eventRepo.findById(event.id)
            assertEquals(null, foundEvent)
        }
    }

    @Test
    fun `should clear repository`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val eventRepo = JdbiEventRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            eventRepo.create(
                title = "Test Event",
                user = user,
                startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime(),
            )
            eventRepo.clear()
            val events = eventRepo.findByUserId(user)
            assertEquals(0, events.size)
        }
    }
}
