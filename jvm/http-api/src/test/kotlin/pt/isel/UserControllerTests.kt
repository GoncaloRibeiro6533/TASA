@file:Suppress("file")

package pt.isel

// Banana

/*
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.controllers.UserController
import pt.isel.models.Problem
import pt.isel.models.user.LoginOutput
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class UserControllerTests {
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
    fun `can create an user, obtain a token, and access user home, and logout`(trxManager: TransactionManager) {
        val controllerUser = UserController(createUserService(trxManager, TestClock()))

        // given: a user
        val name = "Bob"
        val email = "bob@example.com"
        val password = "Tasa_2025"

        // when: creating an user
        // then: the response is a 201
        val userId =
            controllerUser.register(UserRegisterInput(name, email, password)).let { resp ->
                assertEquals(HttpStatus.CREATED, resp.statusCode)
                assertNotNull(resp.body)
                assertIs<User>(resp.body)
                (resp.body as User).id // userId
            }
        // when: creating a token
        // then: the response is a 200
        val token =
            controllerUser.login(UserLoginCredentialsInput(name, password)).let { resp ->
                assertEquals(HttpStatus.OK, resp.statusCode)
                assertIs<LoginOutput>(resp.body)
                (resp.body as LoginOutput).session.token
            }

        // when: getting the user home with a valid token
        // then: the response is a 200 with the proper representation
        val user = User(userId, name, email)
        controllerUser.getUser(AuthenticatedUser(user, token), user.id).also { resp ->
            assertEquals(HttpStatus.OK, resp.statusCode)
            assertEquals(User(userId, name, email), resp.body)
        }
    }

    // Registration failure scenarios

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register fails when username is blank`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.register(UserRegisterInput("  ", "bob@example.com", "Tasa_2025"))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register fails when email is blank`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.register(UserRegisterInput("Bob", "", "Tasa_2025"))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register fails when password is blank`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.register(UserRegisterInput("Bob", "bob@example.com", "   "))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register fails with invalid email format`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.register(UserRegisterInput("Bob", "not-an-email", "Tasa_2025"))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register fails when username already exists`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val name = "Alice"
        // first registration
        controller.register(UserRegisterInput(name, "a1@e.com", "Tasa_2025"))
        // second with same username
        val resp = controller.register(UserRegisterInput(name, "a2@e.com", "Tasa_2025"))
        assertEquals(HttpStatus.CONFLICT, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register fails when email already in use`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val email = "dup@e.com"
        // first registration
        controller.register(UserRegisterInput("user1", email, "Tasa_2025"))
        // second with same email
        val resp = controller.register(UserRegisterInput("user2", email, "Tasa_2025"))
        assertEquals(HttpStatus.CONFLICT, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `register fails with weak password`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.register(UserRegisterInput("Bob", "bob@example.com", "weak"))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    // Login failure scenarios

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `login fails when username is blank`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.login(UserLoginCredentialsInput(" ", "Tasa_2025"))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `login fails when password is blank`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.login(UserLoginCredentialsInput("Bob", " "))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `login fails when username does not exist`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val resp = controller.login(UserLoginCredentialsInput("nouser", "Tasa_2025"))
        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `login fails when password is incorrect`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        // register valid user
        val userInput = UserRegisterInput("Bob", "bob@example.com", "Tasa_2025")
        controller.register(userInput)
        // attempt login with wrong password
        val resp = controller.login(UserLoginCredentialsInput(userInput.username, "WrongPass"))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    // Logout scenarios

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `can logout valid token and then cannot logout again`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        // register and login
        val name = "Bob"
        val email = "bob2@e.com"
        val password = "Tasa_2025"
        val userId = controller.register(UserRegisterInput(name, email, password)).body.let { (it as pt.isel.User).id }
        val token = (controller.login(UserLoginCredentialsInput(name, password)).body as LoginOutput).session.token
        val user = User(userId, name, email)
        // first logout
        val resp1 = controller.logout(AuthenticatedUser(user, token))
        assertEquals(HttpStatus.OK, resp1.statusCode)
        assertNull(resp1.body)
        // second logout should fail (session expired)
        val resp2 = controller.logout(AuthenticatedUser(user, token))
        assertEquals(HttpStatus.UNAUTHORIZED, resp2.statusCode)
        assertIs<Problem>(resp2.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `logout fails when token is blank`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val user = User(1, "Bob", "bob@example.com")
        val resp = controller.logout(AuthenticatedUser(user, "  "))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `logout fails when token format is invalid`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val user = User(1, "Bob", "bob@example.com")
        val resp = controller.logout(AuthenticatedUser(user, "%%%"))
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    // GetUser failure scenarios

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUser fails with negative id`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val user = pt.isel.User(1, "Bob", "bob@example.com")
        val resp = controller.getUser(AuthenticatedUser(user, "token"), -1)
        assertEquals(HttpStatus.BAD_REQUEST, resp.statusCode)
        assertIs<Problem>(resp.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUser fails when user not found`(trxManager: TransactionManager) {
        val controller = UserController(createUserService(trxManager, TestClock()))
        val user = User(1, "Bob", "bob@example.com")
        val resp = controller.getUser(AuthenticatedUser(user, "token"), 999)
        assertEquals(HttpStatus.NOT_FOUND, resp.statusCode)
        assertIs<Problem>(resp.body)
    }
}*/
