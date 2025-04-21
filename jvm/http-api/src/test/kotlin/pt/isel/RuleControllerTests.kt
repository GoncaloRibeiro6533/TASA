package pt.isel

import kotlinx.datetime.Instant
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.controllers.RuleController
import pt.isel.controllers.UserController
import pt.isel.models.rule.RuleEventInput
import pt.isel.models.rule.RuleEventOutput
import pt.isel.models.rule.RuleEventUpdateInput
import pt.isel.models.rule.RuleListOutput
import pt.isel.models.rule.RuleLocationInput
import pt.isel.models.rule.RuleLocationOutput
import pt.isel.models.rule.RuleLocationUpdateInput
import pt.isel.models.user.LoginOutput
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class RuleControllerTests {
    companion object {
        // TODO

        /*private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()
         */

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                // TODO   TransactionManagerJdbi(jdbi).also { cleanup(it) },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                userRepo.clear()
                sessionRepo.clear()
                ruleRepo.clear()
                exclusionRepo.clear()
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

        private fun createRuleService(trxManager: TransactionManager) = RuleService(trxManager)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can create a location rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a location rule input
        val ruleInput =
            RuleLocationInput(
                title = "Home",
                startTime = Instant.parse("2025-01-01T10:00:00Z"),
                endTime = Instant.parse("2025-01-01T12:00:00Z"),
                name = "Home Location",
                latitude = 38.7223,
                longitude = -9.1393,
                radius = 100.0,
            )

        // when: creating a location rule
        // then: the response is a 201 CREATED
        val response = ruleController.createRuleLocation(authUser, ruleInput)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleLocation>(response.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can create an event rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: an event rule input
        val ruleInput =
            RuleEventInput(
                eventId = 1L,
                calendarId = 1L,
                title = "Meeting",
                startTime = Instant.parse("2025-01-01T14:00:00Z"),
                endTime = Instant.parse("2025-01-01T15:00:00Z"),
            )

        // when: creating an event rule
        // then: the response is a 201 CREATED
        val response = ruleController.createRuleEvent(authUser, ruleInput)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleEvent>(response.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can update an event rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a created event rule
        val ruleInput =
            RuleEventInput(
                eventId = 2L,
                calendarId = 2L,
                title = "Team Meeting",
                startTime = Instant.parse("2025-01-02T14:00:00Z"),
                endTime = Instant.parse("2025-01-02T15:00:00Z"),
            )
        val createdRule = ruleController.createRuleEvent(authUser, ruleInput).body as RuleEvent

        // and: update input
        val updateInput =
            RuleEventUpdateInput(
                eventId = 2L,
                calendarId = 2L,
                startTime = Instant.parse("2025-01-02T14:30:00Z"),
                endTime = Instant.parse("2025-01-02T16:00:00Z"),
            )

        // when: updating the event rule
        // then: the response is a 200 OK
        val response = ruleController.updateRuleEvent(authUser, createdRule.id, updateInput)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleEventOutput>(response.body)
        val updatedRule = response.body as RuleEventOutput
        assertEquals(Instant.parse("2025-01-02T14:30:00Z"), updatedRule.startTime)
        assertEquals(Instant.parse("2025-01-02T16:00:00Z"), updatedRule.endTime)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can update a location rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a created location rule
        val ruleInput =
            RuleLocationInput(
                title = "Office",
                startTime = Instant.parse("2025-01-03T09:00:00Z"),
                endTime = Instant.parse("2025-01-03T17:00:00Z"),
                name = "Office Location",
                latitude = 38.7223,
                longitude = -9.1393,
                radius = 150.0,
            )
        val createdRule = ruleController.createRuleLocation(authUser, ruleInput).body as RuleLocation

        // and: update input
        val updateInput =
            RuleLocationUpdateInput(
                startTime = Instant.parse("2025-01-03T08:30:00Z"),
                endTime = Instant.parse("2025-01-03T18:00:00Z"),
            )

        // when: updating the location rule
        // then: the response is a 200 OK
        val response = ruleController.updateRuleLocation(authUser, createdRule.id, updateInput)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleLocation>(response.body)
        val updatedRule = response.body as RuleLocation
        assertEquals(Instant.parse("2025-01-03T08:30:00Z"), updatedRule.startTime)
        assertEquals(Instant.parse("2025-01-03T18:00:00Z"), updatedRule.endTime)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can get a location rule by id`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a created location rule
        val ruleInput =
            RuleLocationInput(
                title = "Gym",
                startTime = Instant.parse("2025-01-04T18:00:00Z"),
                endTime = Instant.parse("2025-01-04T20:00:00Z"),
                name = "Gym Location",
                latitude = 38.7223,
                longitude = -9.1393,
                radius = 50.0,
            )
        val createdRule = ruleController.createRuleLocation(authUser, ruleInput).body as RuleLocation

        // when: getting the location rule by id
        // then: the response is a 200 OK with the proper representation
        val response = ruleController.getRuleLocation(authUser, createdRule.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleLocationOutput>(response.body)
        val retrievedRule = response.body as RuleLocationOutput
        assertEquals(createdRule.id, retrievedRule.id)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can get an event rule by id`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a created event rule
        val ruleInput =
            RuleEventInput(
                eventId = 3L,
                calendarId = 3L,
                title = "Lunch",
                startTime = Instant.parse("2025-01-05T12:00:00Z"),
                endTime = Instant.parse("2025-01-05T13:00:00Z"),
            )
        val createdRule = ruleController.createRuleEvent(authUser, ruleInput).body as RuleEvent

        // when: getting the event rule by id
        // then: the response is a 200 OK with the proper representation
        val response = ruleController.getRuleEvent(authUser, createdRule.id)
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
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: created rules (one location and one event)
        val locationRuleInput =
            RuleLocationInput(
                title = "School",
                startTime = Instant.parse("2025-01-06T08:00:00Z"),
                endTime = Instant.parse("2025-01-06T14:00:00Z"),
                name = "School Location",
                latitude = 38.7223,
                longitude = -9.1393,
                radius = 200.0,
            )
        ruleController.createRuleLocation(authUser, locationRuleInput)

        val eventRuleInput =
            RuleEventInput(
                eventId = 4L,
                calendarId = 4L,
                title = "Project Meeting",
                startTime = Instant.parse("2025-01-06T15:00:00Z"),
                endTime = Instant.parse("2025-01-06T16:00:00Z"),
            )
        ruleController.createRuleEvent(authUser, eventRuleInput)

        // when: getting all rules for the user
        // then: the response is a 200 OK with all rules
        val response = ruleController.getAllRulesFromUser(authUser)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<RuleListOutput>(response.body)
        val ruleList = response.body as RuleListOutput
        assertEquals(1, ruleList.locationRulesN)
        assertEquals(1, ruleList.eventRulesN)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can delete an event rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a created event rule
        val ruleInput =
            RuleEventInput(
                eventId = 5L,
                calendarId = 5L,
                title = "Dinner",
                startTime = Instant.parse("2025-01-07T19:00:00Z"),
                endTime = Instant.parse("2025-01-07T21:00:00Z"),
            )
        val createdRule = ruleController.createRuleEvent(authUser, ruleInput).body as RuleEvent

        // when: deleting the event rule
        // then: the response is a 200 OK
        val deleteResponse = ruleController.deleteRuleEvent(authUser, createdRule.id)
        assertEquals(HttpStatus.OK, deleteResponse.statusCode)

        // and: trying to get the deleted rule should return NOT_FOUND
        val getResponse = ruleController.getRuleEvent(authUser, createdRule.id)
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can delete a location rule`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a created location rule
        val ruleInput =
            RuleLocationInput(
                title = "Library",
                startTime = Instant.parse("2025-01-08T10:00:00Z"),
                endTime = Instant.parse("2025-01-08T16:00:00Z"),
                name = "Library Location",
                latitude = 38.7223,
                longitude = -9.1393,
                radius = 75.0,
            )
        val createdRule = ruleController.createRuleLocation(authUser, ruleInput).body as RuleLocation

        // when: deleting the location rule
        // then: the response is a 200 OK
        val deleteResponse = ruleController.deleteRuleLocation(authUser, createdRule.id)
        assertEquals(HttpStatus.OK, deleteResponse.statusCode)

        // and: trying to get the deleted rule should return NOT_FOUND
        val getResponse = ruleController.getRuleLocation(authUser, createdRule.id)
        assertEquals(HttpStatus.NOT_FOUND, getResponse.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create location rule with invalid radius returns BAD_REQUEST`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a location rule input with invalid radius
        val ruleInput =
            RuleLocationInput(
                title = "Invalid",
                startTime = Instant.parse("2025-01-09T10:00:00Z"),
                endTime = Instant.parse("2025-01-09T12:00:00Z"),
                name = "Invalid Location",
                latitude = 38.7223,
                longitude = -9.1393,
                radius = -10.0,
            )

        // when: creating a location rule
        // then: the response is a 400 BAD_REQUEST
        val response = ruleController.createRuleLocation(authUser, ruleInput)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create rule with end time before start time returns BAD_REQUEST`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: an event rule input with end time before start time
        val ruleInput =
            RuleEventInput(
                eventId = 6L,
                calendarId = 6L,
                title = "Invalid",
                startTime = Instant.parse("2025-01-10T15:00:00Z"),
                endTime = Instant.parse("2025-01-10T14:00:00Z"),
            )

        // when: creating an event rule
        // then: the response is a 400 BAD_REQUEST
        val response = ruleController.createRuleEvent(authUser, ruleInput)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create rule with invalid coordinates returns BAD_REQUEST`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a location rule input with invalid latitude
        val ruleInput =
            RuleLocationInput(
                title = "Invalid",
                startTime = Instant.parse("2025-01-11T10:00:00Z"),
                endTime = Instant.parse("2025-01-11T12:00:00Z"),
                name = "Invalid Location",
                latitude = 95.0,
                longitude = -9.1393,
                radius = 100.0,
            )

        // when: creating a location rule
        // then: the response is a 400 BAD_REQUEST
        val response = ruleController.createRuleLocation(authUser, ruleInput)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getting rule with invalid id returns NOT_FOUND`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // when: getting a rule with non-existent id
        // then: the response is a 404 NOT_FOUND
        val response = ruleController.getRuleEvent(authUser, 9999)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create rule with blank title returns BAD_REQUEST`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a location rule input with blank title
        val ruleInput =
            RuleLocationInput(
                title = "",
                startTime = Instant.parse("2025-01-12T10:00:00Z"),
                endTime = Instant.parse("2025-01-12T12:00:00Z"),
                name = "Invalid Location",
                latitude = 38.7223,
                longitude = -9.1393,
                radius = 100.0,
            )

        // when: creating a location rule
        // then: the response is a 400 BAD_REQUEST
        val response = ruleController.createRuleLocation(authUser, ruleInput)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update rule with time collision returns CONFLICT`(trxManager: TransactionManager) {
        // given: a rule controller and an authenticated user
        val ruleController = RuleController(createRuleService(trxManager))
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: two created rules with different time slots
        val rule1Input =
            RuleEventInput(
                eventId = 7L,
                calendarId = 7L,
                title = "Meeting 1",
                startTime = Instant.parse("2025-01-13T10:00:00Z"),
                endTime = Instant.parse("2025-01-13T11:00:00Z"),
            )
        val createdRule1 = ruleController.createRuleEvent(authUser, rule1Input).body as RuleEvent

        val rule2Input =
            RuleEventInput(
                eventId = 8L,
                calendarId = 8L,
                title = "Meeting 2",
                startTime = Instant.parse("2025-01-13T12:00:00Z"),
                endTime = Instant.parse("2025-01-13T13:00:00Z"),
            )
        val createdRule2 = ruleController.createRuleEvent(authUser, rule2Input).body as RuleEvent

        // and: update input for rule2 that would cause time collision with rule1
        val updateInput =
            RuleEventUpdateInput(
                eventId = 8L,
                calendarId = 8L,
                startTime = Instant.parse("2025-01-13T10:30:00Z"),
                endTime = Instant.parse("2025-01-13T12:30:00Z"),
            )

        // when: updating rule2 to overlap with rule1
        // then: the response is a 409 CONFLICT
        val response = ruleController.updateRuleEvent(authUser, createdRule2.id, updateInput)
        assertEquals(HttpStatus.CONFLICT, response.statusCode)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `trying to access another user's rule returns FORBIDDEN`(trxManager: TransactionManager) {
        // This test would require mocking the service to return NotAllowed error
        // For simplicity, we'll simulate creating a rule for one user and trying to access it with another

        // given: a rule controller
        val ruleController = RuleController(createRuleService(trxManager))

        // and: a user who creates a rule
        val userController = UserController(createUserService(trxManager, TestClock()))
        val user =
            userController.register(
                UserRegisterInput("Bob", "bob@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token =
            userController.login(UserLoginCredentialsInput("Bob", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser = AuthenticatedUser(user, token)

        // and: a created rule
        val ruleInput =
            RuleEventInput(
                eventId = 9L,
                calendarId = 9L,
                title = "Private Meeting",
                startTime = Instant.parse("2025-01-14T10:00:00Z"),
                endTime = Instant.parse("2025-01-14T11:00:00Z"),
            )
        val createdRule = ruleController.createRuleEvent(authUser, ruleInput).body as RuleEvent

        // and: a different user
        val otherUser =
            userController.register(
                UserRegisterInput("Alice", "alice@example.com", "Tasa_2025"),
            ).let { resp ->
                assertNotNull(resp.body)
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertIs<User>(resp.body)
                (resp.body as User)
            }
        var token1 =
            userController.login(UserLoginCredentialsInput("Alice", "Tasa_2025")).let {
                assertNotNull(it.body)
                assertEquals(HttpStatus.OK, it.statusCode)
                assertIs<LoginOutput>(it.body)
                (it.body as LoginOutput).session.token
            }
        val authUser1 = AuthenticatedUser(otherUser, token1)

        // when: the other user tries to access the first user's rule
        val response = ruleController.getRuleEvent(authUser1, createdRule.id)
        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
    }
}
