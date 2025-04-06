import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.Session
import pt.isel.Sha256TokenEncoder
import pt.isel.User
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import session.MockSessionRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours

class MockSessionRepositoryTests {
    private val repo = MockSessionRepository()
    private val clock = TestClock()

    private val usersDomainConfig =
        UsersDomainConfig(
            tokenSizeInBytes = 256 / 8,
            tokenTtl = 24.hours,
            tokenRollingTtl = 1.hours,
            maxTokensPerUser = 3,
        )
    private val tokenEncoder = Sha256TokenEncoder()
    private val usersDomain =
        UsersDomain(
            tokenEncoder = tokenEncoder,
            passwordEncoder = BCryptPasswordEncoder(),
            config = usersDomainConfig,
        )

    private val user =
        User(
            id = 1,
            username = "Bob",
            email = "bob@example.com",
        )

    @BeforeEach
    fun setUp() {
        repo.clear()
    }

    @Test
    fun `create should create a session and return it`() {
        val expected =
            Session(
                token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                refreshToken = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now(),
                expirationDate = clock.now() + usersDomainConfig.tokenTtl,
            )
        val sut =
            repo.createSession(
                user,
                expected.token,
                expected.refreshToken,
                expected.createdAt,
                expected.lastUsedAt,
                expected.expirationDate,
            )
        assertEquals(expected.token, sut.token)
        assertEquals(expected.refreshToken, sut.refreshToken)
        assertEquals(expected.userId, sut.userId)
        assertEquals(expected.createdAt, sut.createdAt)
        assertEquals(expected.lastUsedAt, sut.lastUsedAt)
        assertEquals(expected.expirationDate, sut.expirationDate)
    }

    @Test
    fun `findByToken should return the session with the given token`() {
        val expected =
            Session(
                token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                refreshToken = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now(),
                expirationDate = clock.now() + usersDomainConfig.tokenTtl,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            expected.createdAt,
            expected.lastUsedAt,
            expected.expirationDate,
        )
        val sut = repo.findByToken(expected.token)
        assertEquals(expected, sut)
    }

    @Test
    fun `findByUserId should return the session with the given userId`() {
        val expected =
            Session(
                token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                refreshToken = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now(),
                expirationDate = clock.now() + usersDomainConfig.tokenTtl,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            expected.createdAt,
            expected.lastUsedAt,
            expected.expirationDate,
        )
        val sut = repo.findByUser(user)
        assertEquals(1, sut.size)
        assertEquals(expected, sut[0])
    }

    @Test
    fun `deleteSession should remove the session from the repository`() {
        val expected =
            Session(
                token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                refreshToken = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now(),
                expirationDate = clock.now() + usersDomainConfig.tokenTtl,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            expected.createdAt,
            expected.lastUsedAt,
            expected.expirationDate,
        )
        val deleted = repo.deleteSession(expected)
        assertEquals(true, deleted)
        val sut = repo.findByUser(user)
        assertEquals(0, sut.size)
    }

    @Test
    fun `updateSession should update the session and return it`() {
        val expected =
            Session(
                token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                refreshToken = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now(),
                expirationDate = clock.now() + usersDomainConfig.tokenTtl,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            expected.createdAt,
            expected.lastUsedAt,
            expected.expirationDate,
        )
        val updated = repo.updateSession(expected, clock.now())
        assertEquals(expected.token, updated.token)
        assertEquals(expected.refreshToken, updated.refreshToken)
        assertEquals(expected.userId, updated.userId)
        assertEquals(expected.createdAt, updated.createdAt)
        assertEquals(clock.now(), updated.lastUsedAt)
        assertEquals(expected.expirationDate, updated.expirationDate)
    }

    @Test
    fun `getSessionHistory should return the session history for the given user`() {
        val expected =
            Session(
                token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                refreshToken = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now(),
                expirationDate = clock.now() + usersDomainConfig.tokenTtl,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            expected.createdAt,
            expected.lastUsedAt,
            expected.expirationDate,
        )
        val sut = repo.getSessionHistory(user, 10, 0)
        assertEquals(1, sut.size)
        assertEquals(expected, sut[0])
    }

    @Test
    fun `clear should remove all sessions from the repository`() {
        val expected =
            Session(
                token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                refreshToken = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue()),
                userId = user.id,
                createdAt = clock.now(),
                lastUsedAt = clock.now(),
                expirationDate = clock.now() + usersDomainConfig.tokenTtl,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            expected.createdAt,
            expected.lastUsedAt,
            expected.expirationDate,
        )
        repo.clear()
        val sut = repo.findByUser(user)
        assertEquals(0, sut.size)
    }

    @Test
    fun `getSessionHistory should return an empty list if no sessions exist for the user`() {
        val sut = repo.getSessionHistory(user, 10, 0)
        assertEquals(0, sut.size)
    }

    @Test
    fun `findByToken should return null if the token does not exist`() {
        val token = usersDomain.createTokenValidationInformation(usersDomain.generateTokenValue())
        val sut = repo.findByToken(token)
        assertEquals(null, sut)
    }

    @Test
    fun `findByUserId should return an empty list if the user has no sessions`() {
        val sut = repo.findByUser(user)
        assertEquals(0, sut.size)
    }

    @Test
    fun `getSessionHistory should return an empty list if the user has no sessions`() {
        val sut = repo.getSessionHistory(user, 10, 0)
        assertEquals(0, sut.size)
    }
}
