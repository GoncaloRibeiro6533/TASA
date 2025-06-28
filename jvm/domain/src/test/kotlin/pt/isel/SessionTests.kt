package pt.isel

import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SessionTests {
    @Test
    fun `session creation succeeds`() {
        val encoder = Sha256TokenEncoder()
        val token =
            Token(
                0,
                tokenValidationInfo = encoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                0,
                tokenValidationInfo = encoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        Session(
            0,
            token,
            refreshToken,
            1,
        )
    }

    @Test
    fun `session with negative userId fails`() {
        val encoder = Sha256TokenEncoder()
        val token =
            Token(
                0,
                tokenValidationInfo = encoder.createValidationInformation("token"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                lastUsedAt = "2025-06-01T01:00:00Z".toInstant(),
                expiresAt = "2025-06-03T00:00:00Z".toInstant(),
            )
        val refreshToken =
            RefreshToken(
                0,
                tokenValidationInfo = encoder.createValidationInformation("refreshToken"),
                createdAt = "2025-06-01T00:00:00Z".toInstant(),
                expiresAt = "2025-06-06T01:00:00Z".toInstant(),
            )
        assertFailsWith<IllegalArgumentException> {
            Session(
                0,
                token,
                refreshToken,
                -1,
            )
        }
    }
}
