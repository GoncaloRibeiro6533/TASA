import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.JdbiEventRepository
import pt.isel.JdbiLocationRepository
import pt.isel.JdbiRuleRepository
import pt.isel.JdbiSessionRepository
import pt.isel.JdbiUserRepository
import pt.isel.TokenValidationInfo
import pt.isel.configureWithAppRequirements
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

private fun newTokenValidationData() = "token-${abs(Random.nextLong())}"

class JdbiSessionRepositoryTests {
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
    fun `should create a session`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            val session =
                sessionRepo.createSession(
                    user = user,
                    accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    accessCreatedAt = testClock.now(),
                    accessLastUsedAt = testClock.now(),
                    accessExpiresAt = testClock.now().plus(2.days),
                    maxTokens = 5,
                    refreshCreatedAt = testClock.now(),
                    refreshExpiresAt = testClock.now().plus(30.days),
                )
            assert(session.token.tokenValidationInfo.validationInfo.isNotEmpty())
            assert(session.refreshToken.tokenValidationInfo.validationInfo.isNotEmpty())
            assert(session.userId == user.id)
            val sut =
                JdbiSessionRepository(handle).findByToken(
                    session.token.tokenValidationInfo,
                )
            assertNotNull(sut)
            assert(sut.id == session.id)
            assert(sut.token.id == session.token.id)
            assert(sut.refreshToken.id == session.refreshToken.id)
            assert(sut.userId == session.userId)
            assert(sut.token.tokenValidationInfo.validationInfo == session.token.tokenValidationInfo.validationInfo)
            assert(sut.refreshToken.tokenValidationInfo.validationInfo == session.refreshToken.tokenValidationInfo.validationInfo)
            assert(sut.refreshToken.tokenValidationInfo.validationInfo == session.refreshToken.tokenValidationInfo.validationInfo)
        }
    }

    @Test
    fun `should find a session by its token`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            val session =
                sessionRepo.createSession(
                    user = user,
                    accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    accessCreatedAt = testClock.now(),
                    accessLastUsedAt = testClock.now(),
                    accessExpiresAt = testClock.now().plus(2.days),
                    maxTokens = 5,
                    refreshCreatedAt = testClock.now(),
                    refreshExpiresAt = testClock.now().plus(30.days),
                )
            val sut = sessionRepo.findByToken(session.token.tokenValidationInfo)
            assertNotNull(sut)
            assert(sut.id == session.id)
            assert(sut.token.id == session.token.id)
            assert(sut.refreshToken.id == session.refreshToken.id)
            assert(sut.userId == session.userId)
            assert(sut.token.tokenValidationInfo.validationInfo == session.token.tokenValidationInfo.validationInfo)
            assert(sut.refreshToken.tokenValidationInfo.validationInfo == session.refreshToken.tokenValidationInfo.validationInfo)
            assert(sut.token.createdAt == session.token.createdAt)
            assert(sut.token.lastUsedAt == session.token.lastUsedAt)
        }
    }

    @Test
    fun `should find sessions by user`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            val session1 =
                sessionRepo.createSession(
                    user = user,
                    accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    accessCreatedAt = testClock.now(),
                    accessLastUsedAt = testClock.now(),
                    accessExpiresAt = testClock.now().plus(2.days),
                    maxTokens = 5,
                    refreshCreatedAt = testClock.now(),
                    refreshExpiresAt = testClock.now().plus(30.days),
                )
            val session2 =
                sessionRepo.createSession(
                    user = user,
                    accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    accessCreatedAt = testClock.now(),
                    accessLastUsedAt = testClock.now(),
                    accessExpiresAt = testClock.now().plus(2.days),
                    maxTokens = 5,
                    refreshCreatedAt = testClock.now(),
                    refreshExpiresAt = testClock.now().plus(30.days),
                )
            val sut = sessionRepo.findByUser(user)
            assert(sut.size == 2)
            assert(sut.any { it.id == session1.id && it.userId == user.id })
            assert(sut.any { it.id == session2.id && it.userId == user.id })
            assert(sut.all { it.token.tokenValidationInfo.validationInfo.isNotEmpty() })
            assert(sut.all { it.refreshToken.tokenValidationInfo.validationInfo.isNotEmpty() })
        }
    }

    /*@Test
    fun `should get session history for a user`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user = userRepo.createUser(
                "username",
                "user@example.com",
                "Tasa2025",
            )
            val session1 = sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = 5,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )

            val session2 = sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = 5,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )
            val sut = sessionRepo.getSessionHistory(user, limit = 10, skip = 0)
            assert(sut.size == 2)
            assert(sut.any { it.id == session1.id && it.userId == user.id })
            assert(sut.any { it.id == session2.id && it.userId == user.id })
        }
    }*/

    @Test
    fun `should delete a session by its token`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            val session =
                sessionRepo.createSession(
                    user = user,
                    accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    accessCreatedAt = testClock.now(),
                    accessLastUsedAt = testClock.now(),
                    accessExpiresAt = testClock.now().plus(2.days),
                    maxTokens = 5,
                    refreshCreatedAt = testClock.now(),
                    refreshExpiresAt = testClock.now().plus(30.days),
                )
            val sut = sessionRepo.deleteSession(session)
            assertTrue(sut)
            val foundSession = sessionRepo.findByToken(session.token.tokenValidationInfo)
            assertNull(foundSession)
        }
    }

    @Test
    fun `should update a session's last used time`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            val session =
                sessionRepo.createSession(
                    user = user,
                    accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                    accessCreatedAt = testClock.now(),
                    accessLastUsedAt = testClock.now(),
                    accessExpiresAt = testClock.now().plus(2.days),
                    maxTokens = 5,
                    refreshCreatedAt = testClock.now(),
                    refreshExpiresAt = testClock.now().plus(30.days),
                )
            val lastTimeUsed = testClock.now().plus(1.days)
            val updatedSession =
                sessionRepo.updateSession(
                    session = session,
                    lastTimeUsed = lastTimeUsed,
                )
            assertNotNull(updatedSession)
            assertEquals(session.id, updatedSession.id)
            assertEquals(session.userId, updatedSession.userId)
            assertEquals(session.token.id, updatedSession.token.id)
            assertEquals(session.refreshToken.id, updatedSession.refreshToken.id)
            assertEquals(session.token.tokenValidationInfo.validationInfo, updatedSession.token.tokenValidationInfo.validationInfo)
            assertEquals(
                session.refreshToken.tokenValidationInfo.validationInfo,
                updatedSession.refreshToken.tokenValidationInfo.validationInfo,
            )
            assertEquals(lastTimeUsed, updatedSession.token.lastUsedAt)
        }
    }

    @Test
    fun `should clear all sessions`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = 5,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )
            sessionRepo.clear()
            val sut = sessionRepo.findByUser(user)
            assertTrue(sut.isEmpty())
        }
    }

    @Test
    fun `should not find a session by an token that does not exists`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = 5,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )
            val invalidToken = TokenValidationInfo("invalid-token")
            val sut = sessionRepo.findByToken(invalidToken)
            assertNull(sut)
        }
    }

    @Test
    fun `should delete n tokens when creating a new session if maxTokens is reached`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val sessionRepo = JdbiSessionRepository(handle)

            val user =
                userRepo.createUser(
                    "username",
                    "user@example.com",
                    "Tasa2025",
                )
            val maxTokens = 3
            sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = maxTokens,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )
            sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = maxTokens,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )
            sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = maxTokens,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )
            sessionRepo.createSession(
                user = user,
                accessTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                refreshTokenValidationInfo = TokenValidationInfo(newTokenValidationData()),
                accessCreatedAt = testClock.now(),
                accessLastUsedAt = testClock.now(),
                accessExpiresAt = testClock.now().plus(2.days),
                maxTokens = maxTokens,
                refreshCreatedAt = testClock.now(),
                refreshExpiresAt = testClock.now().plus(30.days),
            )
            val sessions = sessionRepo.findByUser(user)
            assertEquals(maxTokens, sessions.size)
        }
    }
}
