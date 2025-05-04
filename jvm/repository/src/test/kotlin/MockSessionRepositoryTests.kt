import kotlinx.datetime.toInstant
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.RefreshToken
import pt.isel.Session
import pt.isel.Sha256TokenEncoder
import pt.isel.Token
import pt.isel.User
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.session.MockSessionRepository
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
        val token =
            Token(
                tokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                tokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
            )
        val sut =
            repo.createSession(
                user,
                expected.token,
                expected.refreshToken,
                usersDomainConfig.maxTokensPerUser,
            )
        assertEquals(expected.token, sut.token)
        assertEquals(expected.refreshToken, sut.refreshToken)
        assertEquals(expected.userId, sut.userId)
    }

    @Test
    fun `findByToken should return the session with the given token`() {
        val token =
            Token(
                tokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                tokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            usersDomainConfig.maxTokensPerUser,
        )
        val sut = repo.findByToken(expected.token.tokenValidationInfo)
        assertEquals(expected, sut)
    }

    @Test
    fun `findByUserId should return the session with the given userId`() {
        val token =
            Token(
                tokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                tokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            usersDomainConfig.maxTokensPerUser,
        )
        val sut = repo.findByUser(user)
        assertEquals(1, sut.size)
        assertEquals(expected, sut[0])
    }

    @Test
    fun `deleteSession should remove the session from the repository`() {
        val token =
            Token(
                tokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                tokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            usersDomainConfig.maxTokensPerUser,
        )
        val deleted = repo.deleteSession(expected)
        assertEquals(true, deleted)
        val sut = repo.findByUser(user)
        assertEquals(0, sut.size)
    }

    @Test
    fun `updateSession should update the session and return it`() {
        val token =
            Token(
                tokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                tokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            usersDomainConfig.maxTokensPerUser,
        )
        val lastUsedInstant = clock.now()
        val updated = repo.updateSession(expected, lastUsedInstant)
        assertEquals(
            expected.token.tokenValidationInfo.validationInfo,
            updated.token.tokenValidationInfo.validationInfo,
        )
        assertEquals(
            expected.refreshToken.tokenValidationInfo.validationInfo,
            updated.refreshToken.tokenValidationInfo.validationInfo,
        )
        assertEquals(expected.userId, updated.userId)
        assertEquals(expected.token.createdAt, updated.token.createdAt)
        assertEquals(lastUsedInstant, updated.token.lastUsedAt)
    }

    @Test
    fun `getSessionHistory should return the session history for the given user`() {
        val token =
            Token(
                tokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                tokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            usersDomainConfig.maxTokensPerUser,
        )
        val sut = repo.getSessionHistory(user, 10, 0)
        assertEquals(1, sut.size)
        assertEquals(expected, sut[0])
    }

    @Test
    fun `clear should remove all sessions from the repository`() {
        val token =
            Token(
                tokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                tokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = token,
                refreshToken = refreshToken,
                userId = user.id,
            )
        repo.createSession(
            user,
            expected.token,
            expected.refreshToken,
            usersDomainConfig.maxTokensPerUser,
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
