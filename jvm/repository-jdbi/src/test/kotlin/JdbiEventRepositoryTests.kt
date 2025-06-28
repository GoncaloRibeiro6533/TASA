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
            val sessionRepo = JdbiSessionRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    eventId = 1L,
                    calendarId = 1L,
                    title = "Test Event",
                    user = user,
                )
            val createdEvent = eventRepo.findById(event.id, event.calendarId, user)
            assertNotNull(createdEvent)
            assertEquals(event.id, createdEvent.id)
            assertEquals(event.calendarId, createdEvent.calendarId)
            assertEquals(event.title, createdEvent.title)
        }
    }

    @Test
    fun `should find an event by id`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    eventId = 1L,
                    calendarId = 1L,
                    title = "Test Event",
                    user = user,
                )
            val foundEvent = eventRepo.findById(event.id, event.calendarId, user)
            assertNotNull(foundEvent)
            assertEquals(event.id, foundEvent.id)
            assertEquals(event.calendarId, foundEvent.calendarId)
            assertEquals(event.title, foundEvent.title)
        }
    }

    @Test
    fun `should update an event`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    eventId = 1L,
                    calendarId = 1L,
                    title = "Test Event",
                    user = user,
                )
            val updatedEvent =
                eventRepo.update(
                    user,
                    event,
                    newTitle = "Updated Event Title",
                )
            assertNotNull(updatedEvent)
            assertEquals("Updated Event Title", updatedEvent.title)
            val sut = eventRepo.findById(event.id, event.calendarId, user)
            assertNotNull(sut)
            assertEquals("Updated Event Title", sut.title)
        }
    }

    @Test
    fun `should delete an event`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    eventId = 1L,
                    calendarId = 1L,
                    title = "Test Event",
                    user = user,
                )
            val deleted = eventRepo.delete(event = event, user = user)
            assertEquals(true, deleted)
            val foundEvent = eventRepo.findById(event.id, event.calendarId, user)
            assertEquals(null, foundEvent)
        }
    }

    @Test
    fun `should find all events for a user`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)
            val eventRepo = JdbiEventRepository(handle)

            val user = userRepo.createUser("username", "user@example.com", "password")
            eventRepo.create(
                eventId = 1L,
                calendarId = 1L,
                title = "Event 1",
                user = user,
            )
            eventRepo.create(
                eventId = 2L,
                calendarId = 1L,
                title = "Event 2",
                user = user,
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
            val sessionRepo = JdbiSessionRepository(handle)
            val eventRepo = JdbiEventRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            val event =
                eventRepo.create(
                    eventId = 1L,
                    calendarId = 1L,
                    title = "Test Event",
                    user = user,
                )
            val deleted = eventRepo.delete(event = event, user = user)
            assertEquals(true, deleted)
            val foundEvent = eventRepo.findById(event.id, event.calendarId, user)
            assertEquals(null, foundEvent)
        }
    }

    @Test
    fun `should clear repository`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)
            val eventRepo = JdbiEventRepository(handle)
            val user = userRepo.createUser("username", "user@example.com", "password")
            eventRepo.create(
                eventId = 1L,
                calendarId = 1L,
                title = "Test Event",
                user = user,
            )
            eventRepo.clear()
            val events = eventRepo.findByUserId(user)
            assertEquals(0, events.size)
        }
    }
}
