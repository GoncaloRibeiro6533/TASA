package pt.isel

import kotlinx.datetime.Instant
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.time.Duration.Companion.hours

class UsersDomainTests {
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

    @Test
    fun `isValidEmail should return true for valid email`() {
        val result = usersDomain.isValidEmail("bob@mail.com")
        assertTrue(result)
    }

    @Test
    fun `isValidEmail should return false for invalid email`() {
        val result = usersDomain.isValidEmail("bobmail.com")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return true for strong password`() {
        val result = usersDomain.isPasswordStrong("Strong_Password123")
        assertTrue(result)
    }

    @Test
    fun `isPasswordStrong should return false for weak password`() {
        val result = usersDomain.isPasswordStrong("weakpassword")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with no digits`() {
        val result = usersDomain.isPasswordStrong("Weakpassword")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with no lowercase letters`() {
        val result = usersDomain.isPasswordStrong("WEAKPASSWORD123")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with no uppercase letters`() {
        val result = usersDomain.isPasswordStrong("weakpassword123")
        assertTrue(!result)
    }

    @Test
    fun `isPasswordStrong should return false for password with only letters`() {
        val result = usersDomain.isPasswordStrong("Weakpassword")
        assertTrue(!result)
    }

    @Test
    fun `token should be generated`() {
        val result = usersDomain.generateTokenValue()
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `token should be valid`() {
        val token = usersDomain.generateTokenValue()
        val result = usersDomain.canBeToken(token)
        assertTrue(result)
    }

    @Test
    fun `token should be invalid`() {
        val token = "invalidToken"
        val result = usersDomain.canBeToken(token)
        assertTrue(!result)
    }

    @Test
    fun `password should be validated`() {
        val password = "Strong_Password123"
        val validationInfo = usersDomain.createPasswordValidationInformation(password)
        val result = usersDomain.validatePassword(password, validationInfo)
        assertTrue(result)
    }

    @Test
    fun `token expiration should be calculated`() {
        val token =
            Session(
                tokenEncoder.createValidationInformation("token"),
                tokenEncoder.createValidationInformation("refreshToken"),
                userId = 1,
                createdAt = Instant.DISTANT_PAST,
                lastUsedAt = Instant.DISTANT_PAST,
                expirationDate = Instant.DISTANT_FUTURE,
            )
        val result = usersDomain.getSessionExpiration(token)
        assertTrue(result > token.createdAt)
    }

    @Test
    fun `token should be time valid`() {
        val token =
            Session(
                tokenEncoder.createValidationInformation("token"),
                tokenEncoder.createValidationInformation("refreshToken"),
                userId = 0,
                createdAt = clock.now(),
                lastUsedAt = clock.now() + 1.hours,
                expirationDate = clock.now() + 2.hours,
            )
        val result = usersDomain.isSessionTimeValid(clock, token)
        assertTrue(result)
    }

    @Test
    fun `canBeToken fails for invalid token`() {
        val result = usersDomain.canBeToken("invalidToken")
        assertTrue(!result)
    }
}
