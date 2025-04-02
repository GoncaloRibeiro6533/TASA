package pt.isel

import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SessionTests {
    @Test
    fun `session creation succeeds`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        Session(
            token,
            refreshToken,
            1,
            "2025-06-01T00:00:00Z".toInstant(),
            "2025-06-01T01:00:00Z".toInstant(),
            "2025-06-03T00:00:00Z".toInstant(),
        )
    }

    @Test
    fun `session with negative userId fails`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        assertFailsWith<IllegalArgumentException> {
            Session(
                token,
                refreshToken,
                -1,
                "2025-06-01T00:00:00Z".toInstant(),
                "2025-06-01T01:00:00Z".toInstant(),
                "2025-06-03T00:00:00Z".toInstant(),
            )
        }
    }

    @Test
    fun `session with creation date after expiration date fails`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        assertFailsWith<IllegalArgumentException> {
            Session(
                token,
                refreshToken,
                1,
                "2025-06-03T00:00:00Z".toInstant(),
                "2025-06-01T01:00:00Z".toInstant(),
                "2025-06-01T00:00:00Z".toInstant(),
            )
        }
    }

    @Test
    fun `session with lastUsedAt before createdAt fails`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        assertFailsWith<IllegalArgumentException> {
            Session(
                token,
                refreshToken,
                1,
                "2025-06-01T00:00:00Z".toInstant(),
                "2025-05-01T01:00:00Z".toInstant(),
                "2025-06-03T00:00:00Z".toInstant(),
            )
        }
    }

    @Test
    fun `session with lastUsedAt equal to createdAt succeeds`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        Session(
            token,
            refreshToken,
            1,
            "2025-06-01T00:00:00Z".toInstant(),
            "2025-06-01T00:00:00Z".toInstant(),
            "2025-06-03T00:00:00Z".toInstant(),
        )
    }

    @Test
    fun `session with lastUsedAt after createdAt succeeds`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        Session(
            token,
            refreshToken,
            1,
            "2025-06-01T00:00:00Z".toInstant(),
            "2025-06-01T01:00:00Z".toInstant(),
            "2025-06-03T00:00:00Z".toInstant(),
        )
    }

    @Test
    fun `session with lastUsedAt equal to expirationDate succeeds`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        Session(
            token,
            refreshToken,
            1,
            "2025-06-01T00:00:00Z".toInstant(),
            "2025-06-03T00:00:00Z".toInstant(),
            "2025-06-03T00:00:00Z".toInstant(),
        )
    }

    @Test
    fun `session with lastUsedAt after expirationDate fails`() {
        val encoder = Sha256TokenEncoder()
        val token = encoder.createValidationInformation("token")
        val refreshToken = encoder.createValidationInformation("refreshToken")
        assertFailsWith<IllegalArgumentException> {
            Session(
                token,
                refreshToken,
                1,
                "2025-06-01T00:00:00Z".toInstant(),
                "2025-06-03T00:00:00Z".toInstant(),
                "2025-06-01T00:00:00Z".toInstant(),
            )
        }
    }
}
