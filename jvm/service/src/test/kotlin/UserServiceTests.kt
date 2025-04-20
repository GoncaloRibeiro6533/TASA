import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.Failure
import pt.isel.Sha256TokenEncoder
import pt.isel.Success
import pt.isel.TokenExternalInfo
import pt.isel.User
import pt.isel.UserError
import pt.isel.UserService
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.util.stream.Stream
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class UserServiceTests {
    companion object {
        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                // add JDBI TODO
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
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with the valid parameters should create a user`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.register("Bob", "bob@mail.com", "Tasa_2025")
        assertTrue(sut is Success)
        assertIs<User>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with username blank should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.register("", "bob@example.com", "Tasa_2025")
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameCannotBeBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with email blank should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.register("Bob", "", "Tasa_2025")
        assertTrue(sut is Failure)
        assertIs<UserError.EmailCannotBeBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with password blank should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.register("Bob", "bob@example.com", "")
        assertTrue(sut is Failure)
        assertIs<UserError.PasswordCannotBeBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with invalid email format should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.register("Bob", "bobexample.com", "Tasa_2025")
        assertTrue(sut is Failure)
        assertIs<UserError.InvalidEmail>(sut.value)
        val sut2 = service.register("Bob", "bob@example", "Tasa_2025")
        assertTrue(sut2 is Failure)
        assertIs<UserError.InvalidEmail>(sut2.value)
        val sut3 = service.register("Bob", "bob@.com", "Tasa_2025")
        assertTrue(sut3 is Failure)
        assertIs<UserError.InvalidEmail>(sut3.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with email already registered should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user1 = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user1 is Success)
        val sut = service.register("Alice", "bob@example.com", "Tasa_2025")
        assertTrue(sut is Failure)
        assertIs<UserError.EmailAlreadyInUse>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with username too long should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut =
            service.register(
                "Bob" + "b".repeat(User.MAX_USERNAME_LENGTH),
                "bob@example.com",
                "Tasa_2025",
            )
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameToLong>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with username too short should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut =
            service.register(
                "B",
                "bob@example.com",
                "Tasa_2025",
            )
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameToShort>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called and username already exists should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.register("Bob", "alice@example.com", "Tasa_2025")
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameAlreadyExists>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when register is called with a weak password`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.register("Bob", "bob@example.com", "T025")
        assertTrue(sut is Failure)
        assertIs<UserError.WeakPassword>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when login is called with valid parameters should succeed`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.loginUser("Bob", "Tasa_2025")
        assertTrue(sut is Success)
        assertIs<TokenExternalInfo>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when login is called with blank username should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.loginUser("", "Tasa_2025")
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameCannotBeBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when login is called with blank password should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.loginUser("Bob", "")
        assertTrue(sut is Failure)
        assertIs<UserError.PasswordCannotBeBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when login is called with username that does not exists should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.loginUser("Alice", "Tasa_2025")
        assertTrue(sut is Failure)
        assertIs<UserError.NoMatchingUsername>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when login is called and password does not match should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.loginUser("Bob", "Tasa_2026")
        assertTrue(sut is Failure)
        assertIs<UserError.NoMatchingPassword>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when logoutUser is called with a valid token should succeeds`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val session = service.loginUser("Bob", "Tasa_2025")
        assertTrue(session is Success)
        assertIs<TokenExternalInfo>(session.value)
        val sut = service.logoutUser(session.value.token)
        assertTrue(sut is Success)
        assertTrue(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when logoutUser is called with a blank token should return error`() {
        val service =
            createUserService(
                trxManager = TransactionManagerInMem(),
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.logoutUser("")
        assertTrue(sut is Failure)
        assertIs<UserError.InvalidTokenFormat>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when logoutUser is called with a invalid format token should return error`() {
        val service =
            createUserService(
                trxManager = TransactionManagerInMem(),
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.logoutUser("AAAAAAAAAA")
        assertTrue(sut is Failure)
        assertIs<UserError.InvalidTokenFormat>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `when logoutUser is called with a non-existing token should return error`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = service.logoutUser(usersDomain.generateTokenValue())
        assertTrue(sut is Failure)
        assertIs<UserError.SessionExpired>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUserById should return a User with the corresponding identifier`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user =
            service.register(
                "Bob",
                "bob@example.com",
                "Tasa_2025",
            )
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = service.getUserById(user.value.id)
        assertTrue(sut is Success)
        assertIs<User>(sut.value)
        assertEquals(user.value.id, sut.value.id)
        assertEquals(user.value.username, sut.value.username)
        assertEquals(user.value.email, sut.value.email)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUserById should return error when id is negative`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.getUserById(-1)
        assertTrue(sut is Failure)
        assertIs<UserError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUserById should return error when user does not exists`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.getUserById(1)
        assertTrue(sut is Failure)
        assertIs<UserError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `findUserByUsername should return a list of user if any match`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user0 = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user0 is Success)
        assertIs<User>(user0.value)
        val user1 = service.register("bob0", "bob0@example.com", "Tasa_2025")
        assertTrue(user1 is Success)
        assertIs<User>(user1.value)
        val user2 = service.register("BobCat", "bobcat@example.com", "Tasa_2025")
        assertTrue(user2 is Success)
        assertIs<User>(user2.value)
        val sut1 = service.findUserByUsername("B")
        assertTrue(sut1 is Success)
        assertIs<List<User>>(sut1.value)
        assertEquals(3, sut1.value.size)
        assertContains(sut1.value, user0.value)
        assertContains(sut1.value, user1.value)
        assertContains(sut1.value, user2.value)
        val sut2 = service.findUserByUsername("B", 2, 0)
        assertTrue(sut2 is Success)
        assertIs<List<User>>(sut2.value)
        assertEquals(2, sut2.value.size)
        val sut3 = service.findUserByUsername("B", 1, 1)
        assertTrue(sut3 is Success)
        assertIs<List<User>>(sut3.value)
        assertEquals(1, sut3.value.size)
        val sut4 = service.findUserByUsername("B", 2, 1)
        assertTrue(sut4 is Success)
        assertIs<List<User>>(sut4.value)
        assertEquals(2, sut4.value.size)
        val sut5 = service.findUserByUsername("B", 0, 1)
        assertTrue(sut5 is Success)
        assertIs<List<User>>(sut5.value)
        assertEquals(0, sut5.value.size)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateUsername should succeeds if given valid parameters`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = service.updateUsername(user.value.id, "BobCat")
        assertTrue(sut is Success)
        assertIs<User>(sut.value)
        assertEquals(user.value.id, sut.value.id)
        assertEquals("BobCat", sut.value.username)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateUsername should return error if new username is blank`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = service.updateUsername(user.value.id, "")
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameCannotBeBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateUsername should return error if newUsername has wrong length`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = service.updateUsername(user.value.id, "B")
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameToShort>(sut.value)
        val sut1 = service.updateUsername(user.value.id, "Bob" + "b".repeat(User.MAX_USERNAME_LENGTH))
        assertTrue(sut1 is Failure)
        assertIs<UserError.UsernameToLong>(sut1.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update username should return error if newUsername already exists`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val user2 = service.register("Alice", "alice@example.com", "Tasa_2025")
        assertTrue(user2 is Success)
        assertIs<User>(user2.value)
        val sut = service.updateUsername(user.value.id, "Alice")
        assertTrue(sut is Failure)
        assertIs<UserError.UsernameAlreadyExists>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteUser should succeed if given existing user and valid id`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = service.deleteUser(user.value.id)
        assertTrue(sut is Success)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteUser should return error if given negative id`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.deleteUser(-1)
        assertTrue(sut is Failure)
        assertIs<UserError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteUser should return error if given non-existing user`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.deleteUser(1)
        assertTrue(sut is Failure)
        assertIs<UserError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUserByToken should succeed if token is valid and session is valid`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val user = service.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val session = service.loginUser("Bob", "Tasa_2025")
        assertTrue(session is Success)
        val sut = service.getUserByToken(session.value.token)
        assertNotNull(sut)
        assertIs<User>(sut)
        assertEquals(user.value.id, sut.id)
        assertEquals(user.value.username, sut.username)
        assertEquals(user.value.email, sut.email)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUserByToken should return null if token is blank`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.getUserByToken("")
        assertNull(sut)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUserByToken should return null if token is invalid`(trxManager: TransactionManager) {
        val service =
            createUserService(
                trxManager = trxManager,
                testClock = TestClock(),
            )
        val sut = service.getUserByToken("AAAAAAAAAA")
        assertNull(sut)
    }
}
