import kotlinx.datetime.toInstant
import org.junit.jupiter.api.BeforeEach
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.Session
import pt.isel.Sha256TokenEncoder
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
        val sut =
            repo.createSession(
                user = user,
                accessTokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                accessCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                accessLastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                accessExpiresAt = "2025-06-03T00:00:00Z".toInstant(),
                maxTokens = usersDomainConfig.maxTokensPerUser,
                refreshTokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                refreshCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                refreshExpiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = sut.token,
                refreshToken = sut.refreshToken,
                userId = user.id,
            )
        assertEquals(expected.token, sut.token)
        assertEquals(expected.refreshToken, sut.refreshToken)
        assertEquals(expected.userId, sut.userId)
    }

    @Test
    fun `findByToken should return the session with the given token`() {
        val session =
            repo.createSession(
                user = user,
                accessTokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                accessCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                accessLastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                accessExpiresAt = "2025-06-03T00:00:00Z".toInstant(),
                maxTokens = usersDomainConfig.maxTokensPerUser,
                refreshTokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                refreshCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                refreshExpiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val sut = repo.findByToken(session.token.tokenValidationInfo)
        assertEquals(session, sut)
    }

    @Test
    fun `findByUserId should return the session with the given userId`() {
        val session =
            repo.createSession(
                user = user,
                accessTokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                accessCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                accessLastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                accessExpiresAt = "2025-06-03T00:00:00Z".toInstant(),
                maxTokens = usersDomainConfig.maxTokensPerUser,
                refreshTokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                refreshCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                refreshExpiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = session.token,
                refreshToken = session.refreshToken,
                userId = user.id,
            )
        val sut = repo.findByUser(user)
        assertEquals(1, sut.size)
        assertEquals(expected, sut[0])
    }

    @Test
    fun `deleteSession should remove the session from the repository`() {
        val session =
            repo.createSession(
                user = user,
                accessTokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                accessCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                accessLastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                accessExpiresAt = "2025-06-03T00:00:00Z".toInstant(),
                maxTokens = usersDomainConfig.maxTokensPerUser,
                refreshTokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                refreshCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                refreshExpiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = session.token,
                refreshToken = session.refreshToken,
                userId = user.id,
            )
        val deleted = repo.deleteSession(expected)
        assertEquals(true, deleted)
        val sut = repo.findByUser(user)
        assertEquals(0, sut.size)
    }

    @Test
    fun `updateSession should update the session and return it`() {
        val session =
            repo.createSession(
                user = user,
                accessTokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                accessCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                accessLastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                accessExpiresAt = "2025-06-03T00:00:00Z".toInstant(),
                maxTokens = usersDomainConfig.maxTokensPerUser,
                refreshTokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                refreshCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                refreshExpiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = session.token,
                refreshToken = session.refreshToken,
                userId = user.id,
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
        val session =
            repo.createSession(
                user = user,
                accessTokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                accessCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                accessLastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                accessExpiresAt = "2025-06-03T00:00:00Z".toInstant(),
                maxTokens = usersDomainConfig.maxTokensPerUser,
                refreshTokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                refreshCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                refreshExpiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = session.token,
                refreshToken = session.refreshToken,
                userId = user.id,
            )
        val sut = repo.getSessionHistory(user, 10, 0)
        assertEquals(1, sut.size)
        assertEquals(expected, sut[0])
    }

    @Test
    fun `clear should remove all sessions from the repository`() {
        val session =
            repo.createSession(
                user = user,
                accessTokenValidationInfo = tokenEncoder.createValidationInformation("token"),
                accessCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                accessLastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                accessExpiresAt = "2025-06-03T00:00:00Z".toInstant(),
                maxTokens = usersDomainConfig.maxTokensPerUser,
                refreshTokenValidationInfo = tokenEncoder.createValidationInformation("refreshToken"),
                refreshCreatedAt = "2025-06-01T00:00:00Z".toInstant(),
                refreshExpiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        val expected =
            Session(
                id = 0,
                token = session.token,
                refreshToken = session.refreshToken,
                userId = user.id,
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
