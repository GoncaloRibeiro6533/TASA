package pt.isel

import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours

class UsersDomainConfigTests {
    @Test
    fun `UsersDomainConfig creation succeeds with valid parameters`() {
        UsersDomainConfig(
            8,
            2.hours,
            1.hours,
            5,
        )
    }

    @Test
    fun `UsersDomainConfig creation fails with invalid tokenSizeInBytes`() {
        assertFailsWith<IllegalArgumentException> {
            UsersDomainConfig(
                0,
                2.hours,
                1.hours,
                5,
            )
        }
    }

    @Test
    fun `UsersDomainConfig creation fails with invalid tokenTtl`() {
        assertFailsWith<IllegalArgumentException> {
            UsersDomainConfig(
                8,
                (-2).hours,
                1.hours,
                5,
            )
        }
    }

    @Test
    fun `UsersDomainConfig creation fails with invalid tokenRollingTtl`() {
        assertFailsWith<IllegalArgumentException> {
            UsersDomainConfig(
                8,
                2.hours,
                (-1).hours,
                5,
            )
        }
    }

    @Test
    fun `UsersDomainConfig creation fails with invalid maxTokensPerUser`() {
        assertFailsWith<IllegalArgumentException> {
            UsersDomainConfig(
                8,
                2.hours,
                1.hours,
                0,
            )
        }
    }
}
