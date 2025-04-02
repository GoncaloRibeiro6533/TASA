package pt.isel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserTests {
    @Test
    fun `user creation succeeds`() {
        val user = User(1, "Bob", "bob@example.com")
        assertEquals(1, user.id)
        assertEquals("Bob", user.username)
        assertEquals("bob@example.com", user.email)
    }

    @Test
    fun `user id must be a positive number`() {
        assertFailsWith<IllegalArgumentException> {
            User(-1, "Bob", "bob@example.com")
        }
    }

    @Test
    fun `username must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            User(1, "", "bob@example.com")
        }
    }

    @Test
    fun `email must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            User(1, "Bob", "")
        }
    }

    @Test
    fun `username must not be longer than max username length`() {
        assertFailsWith<IllegalArgumentException> {
            User(1, "A".repeat(User.MAX_USERNAME_LENGTH + 1), "bob@example.com")
        }
    }

    @Test
    fun `email must not be longer than max email length`() {
        assertFailsWith<IllegalArgumentException> {
            User(1, "Bob", "bob@example.com" + "m".repeat(User.MAX_EMAIL_LENGTH))
        }
    }

    @Test
    fun `email must be valid`() {
        assertFailsWith<IllegalArgumentException> {
            User(1, "Bob", "bob@example")
        }
        assertFailsWith<IllegalArgumentException> {
            User(1, "Bob", "bob.example.com")
        }
        assertFailsWith<IllegalArgumentException> {
            User(1, "Bob", "bob@example.")
        }
        assertFailsWith<IllegalArgumentException> {
            User(1, "Bob", "bobexamplec")
        }
    }
}
