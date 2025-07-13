package pt.isel

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.controllers.EventController
import pt.isel.errorHandlers.EventErrorHandler
import pt.isel.models.event.EventInput
import pt.isel.models.event.EventUpdateInput
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.time.LocalDateTime
import java.util.Locale
import java.util.stream.Stream
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

class EventControllerTests {
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
                    refreshTime = 60.days,
                ),
            )

        private fun createEventErrorHandler(): EventErrorHandler =
            EventErrorHandler(
                messageSource = createTestMessageSource(),
            )

        private fun createEventController(eventService: EventService) = EventController(eventService, createEventErrorHandler())
    }

    @BeforeEach
    fun setup() {
        Locale.setDefault(Locale.ENGLISH)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createEvent should create a new event`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val controllerEvents = createEventController(EventService(trxManager))
        val eventInput =
            EventInput(
                title = "Team Meeting",
                startTime = LocalDateTime.of(2025, 10, 1, 10, 0, 0),
                endTime = LocalDateTime.of(2025, 10, 1, 11, 0, 0),
            )
        val response = controllerEvents.createEvent(AuthenticatedUser(rose, newTokenValidationData()), eventInput)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getEvent should return the correct event`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val event =
            trxManager.run {
                eventRepo.create(
                    "Team Meeting",
                    rose,
                    LocalDateTime.of(2025, 10, 1, 10, 0, 0),
                    LocalDateTime.of(2025, 10, 1, 11, 0, 0),
                )
            }
        val controllerEvents = createEventController(EventService(trxManager))
        val response = controllerEvents.getEvent(AuthenticatedUser(rose, newTokenValidationData()), event.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<Event>(response.body)
        assertEquals("Team Meeting", (response.body as Event).title)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateEvent should update the event title`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val event =
            trxManager.run {
                eventRepo.create(
                    "Team Meeting",
                    rose,
                    LocalDateTime.of(2025, 10, 1, 10, 0, 0),
                    LocalDateTime.of(2025, 10, 1, 11, 0, 0),
                )
            }
        val controllerEvents = createEventController(EventService(trxManager))
        val eventInput =
            EventUpdateInput(
                newTitle = "Updated Meeting",
            )
        val response = controllerEvents.updateEvent(AuthenticatedUser(rose, newTokenValidationData()), eventInput, event.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<Event>(response.body)
        assertEquals("Updated Meeting", (response.body as Event).title)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteEvent should delete the event`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val event =
            trxManager.run {
                eventRepo.create(
                    "Team Meeting",
                    rose,
                    LocalDateTime.of(2025, 10, 1, 10, 0, 0),
                    LocalDateTime.of(2025, 10, 1, 11, 0, 0),
                )
            }
        val controllerEvents = createEventController(EventService(trxManager))
        val response = controllerEvents.deleteEvent(AuthenticatedUser(rose, newTokenValidationData()), event.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNull(response.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getAllEvents should return a list of events`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        trxManager.run {
            eventRepo.create(
                "Team Meeting",
                rose,
                LocalDateTime.of(2025, 10, 1, 10, 0, 0),
                LocalDateTime.of(2025, 10, 1, 11, 0, 0),
            )
        }
        trxManager.run {
            eventRepo.create(
                "Project Kickoff",
                rose,
                LocalDateTime.of(2025, 10, 2, 9, 0, 0),
                LocalDateTime.of(2025, 10, 2, 10, 0, 0),
            )
        }
        val controllerEvents = createEventController(EventService(trxManager))
        val response = controllerEvents.getAllEvents(AuthenticatedUser(rose, newTokenValidationData()))
        val body = response.body
        assertNotNull(body)
        assertIs<List<Event>>(body)
        assertEquals(2, body.size)
    }
}
