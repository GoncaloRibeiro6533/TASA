package pt.isel

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.controllers.RuleController
import pt.isel.errorHandlers.RuleErrorHandler
import pt.isel.models.rule.RuleEventInput
import pt.isel.models.rule.RuleEventOutput
import pt.isel.models.rule.RuleEventUpdateInput
import pt.isel.models.rule.RuleListOutput
import pt.isel.models.rule.RuleLocationInput
import pt.isel.models.rule.RuleLocationOutput
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.time.LocalDateTime
import java.util.Locale
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class RuleControllerTests {
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
                    1.hours,
                ),
            )

        private fun createRuleService(trxManager: TransactionManager) = RuleService(trxManager)

        private fun createLocationErrorHandler(): RuleErrorHandler =
            RuleErrorHandler(
                messageSource = createTestMessageSource(),
            )
    }

    private fun createRuleController(ruleService: RuleService) = RuleController(ruleService, createLocationErrorHandler())

    @BeforeEach
    fun setup() {
        Locale.setDefault(Locale.ENGLISH)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can create a location rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val location =
            trxManager.run {
                locationRepo.create(
                    user = user,
                    name = "Home",
                    latitude = 38.7223,
                    longitude = -9.1393,
                    radius = 100.0,
                )
            }
        // and: a location rule input
        val ruleInput =
            RuleLocationInput(
                locationId = location.id,
            )

        // when: creating a location rule
        // then: the response is a 201 CREATED
        val response = ruleController.createRuleLocation(AuthenticatedUser(user, newTokenValidationData()), ruleInput)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleLocation>(response.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can create an event rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
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
                    user = user,
                    title = "Meeting",
                    startTime = LocalDateTime.parse("2025-01-01T14:00:00"),
                    endTime = LocalDateTime.parse("2025-01-01T15:00:00"),
                )
            }
        // and: an event rule input
        val ruleInput =
            RuleEventInput(
                eventId = event.id,
                startTime = LocalDateTime.parse("2025-01-01T14:00:00"),
                endTime = LocalDateTime.parse("2025-01-01T15:00:00"),
            )

        // when: creating an event rule
        // then: the response is a 201 CREATED
        val response = ruleController.createRuleEvent(AuthenticatedUser(user, newTokenValidationData()), ruleInput)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleEvent>(response.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can update an event rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
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
                    user = user,
                    title = "Team Meeting",
                    startTime = LocalDateTime.parse("2025-01-02T14:00:00"),
                    endTime = LocalDateTime.parse("2025-01-02T15:00:00"),
                )
            }
        // and: a created event rule
        val ruleInput =
            RuleEventInput(
                eventId = event.id,
                startTime = LocalDateTime.parse("2025-01-02T14:00:00"),
                endTime = LocalDateTime.parse("2025-01-02T15:00:00"),
            )
        val createdRule =
            ruleController.createRuleEvent(
                AuthenticatedUser(user, newTokenValidationData()),
                ruleInput,
            ).body as RuleEvent

        // and: update input
        val updateInput =
            RuleEventUpdateInput(
                startTime = LocalDateTime.parse("2025-01-02T14:30:00"),
                endTime = LocalDateTime.parse("2025-01-02T16:00:00"),
            )

        // when: updating the event rule
        // then: the response is a 200 OK
        val response = ruleController.updateRuleEvent(AuthenticatedUser(user, newTokenValidationData()), createdRule.id, updateInput)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleEventOutput>(response.body)
        val updatedRule = response.body as RuleEventOutput
        assertEquals(LocalDateTime.parse("2025-01-02T14:30:00"), updatedRule.startTime)
        assertEquals(LocalDateTime.parse("2025-01-02T16:00:00"), updatedRule.endTime)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can get a location rule by id`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val location =
            trxManager.run {
                locationRepo.create(
                    user = user,
                    name = "Home",
                    latitude = 38.7223,
                    longitude = -9.1393,
                    radius = 100.0,
                )
            }
        // and: a created location rule
        val ruleInput =
            RuleLocationInput(location.id)
        val createdRule =
            ruleController.createRuleLocation(
                AuthenticatedUser(user, newTokenValidationData()),
                ruleInput,
            ).body as RuleLocation

        // when: getting the location rule by id
        // then: the response is a 200 OK with the proper representation
        val response = ruleController.getRuleLocation(AuthenticatedUser(user, newTokenValidationData()), createdRule.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        assertNotNull(body)
        assertIs<RuleLocationOutput>(body)
        assertEquals(createdRule.id, body.id)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can get an event rule by id`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
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
                    user = user,
                    title = "Lunch Meeting",
                    startTime = LocalDateTime.parse("2025-01-05T12:00:00"),
                    endTime = LocalDateTime.parse("2025-01-05T13:00:00"),
                )
            }
        // and: a created event rule
        val ruleInput =
            RuleEventInput(
                eventId = event.id,
                startTime = LocalDateTime.parse("2025-01-05T12:00:00"),
                endTime = LocalDateTime.parse("2025-01-05T13:00:00"),
            )
        val createdRule =
            trxManager.run {
                ruleRepo.createEventRule(
                    event,
                    user,
                    startTime = ruleInput.startTime,
                    endTime = ruleInput.endTime,
                )
            }

        // when: getting the event rule by id
        // then: the response is a 200 OK with the proper representation
        val response = ruleController.getRuleEvent(AuthenticatedUser(user, newTokenValidationData()), createdRule.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleEventOutput>(response.body)
        val retrievedRule = response.body as RuleEventOutput
        assertEquals(createdRule.id, retrievedRule.id)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can get all rules from user`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Rose Mary",
                    "rose@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser = AuthenticatedUser(user, newTokenValidationData())

        val location =
            trxManager.run {
                locationRepo.create(
                    user = user,
                    name = "Home",
                    latitude = 38.7223,
                    longitude = -9.1393,
                    radius = 100.0,
                )
            }
        val event =
            trxManager.run {
                eventRepo.create(
                    user = user,
                    title = "Project Meeting",
                    startTime = LocalDateTime.parse("2025-01-06T15:00:00"),
                    endTime = LocalDateTime.parse("2025-01-06T16:00:00"),
                )
            }

        // and: created rules (one location and one event)
        val locationRuleInput = RuleLocationInput(location.id)
        ruleController.createRuleLocation(authUser, locationRuleInput)

        val ruleInput =
            RuleEventInput(
                eventId = event.id,
                startTime = LocalDateTime.parse("2025-01-06T15:00:00"),
                endTime = LocalDateTime.parse("2025-01-06T16:00:00"),
            )
        ruleController.createRuleEvent(authUser, ruleInput)

        // when: getting all rules for the user
        val response = ruleController.getAllRulesFromUser(authUser)

        // then: the response is a 200 OK with all rules
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleListOutput>(response.body)
        val ruleList = response.body as RuleListOutput
        assertEquals(1, ruleList.locationRules.size)
        assertEquals(1, ruleList.eventRules.size)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can delete an event rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Bob",
                    "bob@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser = AuthenticatedUser(user, newTokenValidationData())

        // and: an event
        val event =
            trxManager.run {
                eventRepo.create(
                    user = user,
                    title = "Dinner",
                    startTime = LocalDateTime.parse("2025-01-07T19:00:00"),
                    endTime = LocalDateTime.parse("2025-01-07T21:00:00"),
                )
            }

        // and: a created event rule
        val ruleInput =
            RuleEventInput(
                eventId = event.id,
                startTime = LocalDateTime.parse("2025-01-07T19:00:00"),
                endTime = LocalDateTime.parse("2025-01-07T21:00:00"),
            )
        val createdRule = ruleController.createRuleEvent(authUser, ruleInput).body as RuleEvent

        // when: deleting the event rule
        val deleteResponse = ruleController.deleteRuleEvent(authUser, createdRule.id)

        // then: the response is a 200 OK
        assertEquals(HttpStatus.OK, deleteResponse.statusCode)

        // and: trying to get the deleted rule should return NOT_FOUND
        val getResponse = ruleController.getRuleEvent(authUser, createdRule.id)
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can delete a location rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Bob",
                    "bob@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser = AuthenticatedUser(user, newTokenValidationData())

        // and: a location
        val location =
            trxManager.run {
                locationRepo.create(
                    user = user,
                    name = "Library",
                    latitude = 38.7223,
                    longitude = -9.1393,
                    radius = 75.0,
                )
            }

        // and: a created location rule
        val ruleInput = RuleLocationInput(location.id)
        val createdRule = ruleController.createRuleLocation(authUser, ruleInput).body as RuleLocation

        // when: deleting the location rule
        val deleteResponse = ruleController.deleteRuleLocation(authUser, createdRule.id)

        // then: the response is a 200 OK
        assertEquals(HttpStatus.OK, deleteResponse.statusCode)

        // and: trying to get the deleted rule should return NOT_FOUND
        val getResponse = ruleController.getRuleLocation(authUser, createdRule.id)
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create rule with end time before start time returns BAD_REQUEST`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Bob",
                    "bob@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser = AuthenticatedUser(user, newTokenValidationData())

        // and: an event

        val event =
            trxManager.run {
                eventRepo.create(
                    user = user,
                    title = "Invalid Event",
                    startTime = LocalDateTime.parse("2025-01-10T15:00:00"),
                    endTime = LocalDateTime.parse("2025-01-10T16:00:00"),
                )
            }

        // and: an event rule input with end time before start time

        val ruleInput =
            RuleEventInput(
                eventId = event.id,
                startTime = LocalDateTime.parse("2025-01-10T15:00:00"),
                endTime = LocalDateTime.parse("2025-01-10T14:00:00"),
            )

        // when: creating an event rule
        val response = ruleController.createRuleEvent(authUser, ruleInput)

        // then: the response is a 400 BAD_REQUEST
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getting rule with invalid id returns NOT_FOUND`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Bob",
                    "bob@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser = AuthenticatedUser(user, newTokenValidationData())

        // when: getting a rule with non-existent id
        val response = ruleController.getRuleEvent(authUser, 9999)

        // then: the response is a 404 NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update rule with time collision returns CONFLICT`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = createRuleController(createRuleService(trxManager))
        val user =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Bob",
                    "bob@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser = AuthenticatedUser(user, newTokenValidationData())

        // and: two events
        val event1 =
            trxManager.run {
                eventRepo.create(
                    user = user,
                    title = "Meeting 1",
                    startTime = LocalDateTime.parse("2025-01-13T10:00:00"),
                    endTime = LocalDateTime.parse("2025-01-13T11:00:00"),
                )
            }
        val event2 =
            trxManager.run {
                eventRepo.create(
                    user = user,
                    title = "Meeting 2",
                    startTime = LocalDateTime.parse("2025-01-13T12:00:00"),
                    endTime = LocalDateTime.parse("2025-01-13T13:00:00"),
                )
            }

        // and: two created rules with different time slots
        val rule1Input =
            RuleEventInput(
                eventId = event1.id,
                startTime = LocalDateTime.parse("2025-01-13T10:00:00"),
                endTime = LocalDateTime.parse("2025-01-13T11:00:00"),
            )
        ruleController.createRuleEvent(authUser, rule1Input).body as RuleEvent

        val rule2Input =
            RuleEventInput(
                eventId = event2.id,
                startTime = LocalDateTime.parse("2025-01-13T12:00:00"),
                endTime = LocalDateTime.parse("2025-01-13T13:00:00"),
            )
        val createdRule2 = ruleController.createRuleEvent(authUser, rule2Input).body as RuleEvent

        // and: update input for rule2 that would cause time collision with rule1
        val updateInput =
            RuleEventUpdateInput(
                startTime = LocalDateTime.parse("2025-01-13T10:30:00"),
                endTime = LocalDateTime.parse("2025-01-13T12:30:00"),
            )

        // when: updating rule2 to overlap with rule1
        val response = ruleController.updateRuleEvent(authUser, createdRule2.id, updateInput)

        // then: the response is a 409 CONFLICT
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `trying to access another user's rule returns FORBIDDEN`(trxManager: TransactionManager) {
        // given: a rule controller
        val ruleController = createRuleController(createRuleService(trxManager))

        // and: two users
        val user1 =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Bob",
                    "bob@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser1 = AuthenticatedUser(user1, newTokenValidationData())

        val user2 =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser(
                    "Alice",
                    "alice@example.com",
                    passwordValidationInfo.validationInfo,
                )
            }
        val authUser2 = AuthenticatedUser(user2, newTokenValidationData())

        // and: an event for user1
        val event =
            trxManager.run {
                eventRepo.create(
                    user = user1,
                    title = "Private Meeting",
                    startTime = LocalDateTime.parse("2025-01-14T10:00:00"),
                    endTime = LocalDateTime.parse("2025-01-14T11:00:00"),
                )
            }

        // and: a created rule for user1
        val ruleInput =
            RuleEventInput(
                eventId = event.id,
                startTime = LocalDateTime.parse("2025-01-14T10:00:00"),
                endTime = LocalDateTime.parse("2025-01-14T11:00:00"),
            )
        val createdRule = ruleController.createRuleEvent(authUser1, ruleInput).body as RuleEvent

        // when: user2 tries to access user1's rule
        val response = ruleController.getRuleEvent(authUser2, createdRule.id)

        // then: the response is a 403 FORBIDDEN
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
}
