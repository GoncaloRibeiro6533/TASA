import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.Event
import pt.isel.EventError
import pt.isel.EventService
import pt.isel.Failure
import pt.isel.Sha256TokenEncoder
import pt.isel.Success
import pt.isel.TransactionManagerJdbi
import pt.isel.User
import pt.isel.UserService
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.configureWithAppRequirements
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class EventServiceTests {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                TransactionManagerJdbi(jdbi).also { cleanup(it) },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                userRepo.clear()
                sessionRepo.clear()
                ruleRepo.clear()
                eventRepo.clear()
                locationRepo.clear()
            }
        }

        private val usersDomain =
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    tokenSizeInBytes = 256 / 8,
                    tokenTtl = 30.days,
                    tokenRollingTtl = 30.minutes,
                    maxTokensPerUser = 3,
                    10.hours,
                ),
            )

        private fun createUserService(
            trxManager: TransactionManager,
            testClock: TestClock,
        ) = UserService(
            trxManager,
            usersDomain,
            testClock,
        )
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create event should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create event should return error if title is blank`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Failure)
        assertIs<EventError.EventNameCannotBeBlank>(event.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create event should return error if event exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val sut =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(sut is Failure)
        assertIs<EventError.AlreadyExists>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create event should return error if user not found`(trxManager: TransactionManager) {
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = 99999,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Failure)
        assertIs<EventError.UserNotFound>(event.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateEvent should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val updatedEvent =
            eventService.updateEvent(
                eventId = event.value.id,
                newTitle = "Updated Event",
                userId = user.value.id,
            )
        assertTrue(updatedEvent is Success)
        assertIs<Event>(updatedEvent.value)
        assertEquals(event.value.id, updatedEvent.value.id)
        assertEquals("Updated Event", updatedEvent.value.title)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateEvent should return error if eventId is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val updatedEvent =
            eventService.updateEvent(
                eventId = -1,
                newTitle = "Updated Event",
                userId = user.value.id,
            )
        assertTrue(updatedEvent is Failure)
        assertIs<EventError.NegativeIdentifier>(updatedEvent.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateEvent should return error if newTitle is blank`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val updatedEvent =
            eventService.updateEvent(
                eventId = event.value.id,
                newTitle = "",
                userId = user.value.id,
            )
        assertTrue(updatedEvent is Failure)
        assertIs<EventError.EventNameCannotBeBlank>(updatedEvent.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateEvent should return error if user not found`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val updatedEvent =
            eventService.updateEvent(
                eventId = 1,
                newTitle = "Updated Event",
                userId = 9999,
            )
        assertTrue(updatedEvent is Failure)
        assertIs<EventError.UserNotFound>(updatedEvent.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update event should return error if event not found`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val updatedEvent =
            eventService.updateEvent(
                eventId = 1,
                newTitle = "Updated Event",
                userId = user.value.id,
            )
        assertTrue(updatedEvent is Failure)
        assertIs<EventError.EventNotFound>(updatedEvent.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getEventsOfUser should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val sut = eventService.getEventsOfUser(user.value.id)
        assertTrue(sut is Success)
        assertIs<List<Event>>(sut.value)
        assertEquals(1, sut.value.size)
        assertTrue(sut.value.contains(event.value))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getEventsOfUser should return error if userId is negative`(trxManager: TransactionManager) {
        val eventService = EventService(trxManager)
        val sut = eventService.getEventsOfUser(-1)
        assertTrue(sut is Failure)
        assertIs<EventError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getEventsOfUser should return error if user not found`(trxManager: TransactionManager) {
        val eventService = EventService(trxManager)
        val sut = eventService.getEventsOfUser(9999)
        assertTrue(sut is Failure)
        assertIs<EventError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteEvent should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val sut =
            eventService.deleteEvent(
                eventId = event.value.id,
                userId = user.value.id,
            )
        assertTrue(sut is Success)
        assertIs<Boolean>(sut.value)
        assertTrue(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteEvent should return error if eventId is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val sut =
            eventService.deleteEvent(
                eventId = -1,
                userId = user.value.id,
            )
        assertTrue(sut is Failure)
        assertIs<EventError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteEvent should return error if user id is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val sut =
            eventService.deleteEvent(
                eventId = 1,
                userId = -1,
            )
        assertTrue(sut is Failure)
        assertIs<EventError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteEvent should return error if user does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val event =
            eventService.createEvent(
                title = "Test Event",
                userId = user.value.id,
                startTime = "2025-11-01T00:00:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-11-01T01:00:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        assertTrue(event is Success)
        assertIs<Event>(event.value)
        val sut =
            eventService.deleteEvent(
                eventId = 1,
                userId = 9999,
            )
        assertTrue(sut is Failure)
        assertIs<EventError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteEvent should return error if event does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val eventService = EventService(trxManager)
        val sut =
            eventService.deleteEvent(
                eventId = 1,
                userId = user.value.id,
            )
        assertTrue(sut is Failure)
        assertIs<EventError.EventNotFound>(sut.value)
    }
}
