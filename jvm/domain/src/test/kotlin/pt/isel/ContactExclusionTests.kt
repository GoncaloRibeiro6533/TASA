package pt.isel

import kotlin.test.Test
import kotlin.test.assertFailsWith

class ContactExclusionTests {
    @Test
    fun `contact exclusion succeeds`() {
        ContactExclusion(1, "Bob", "+351911111111")
    }

    @Test
    fun `id must be a positive number`() {
        assertFailsWith<IllegalArgumentException> {
            ContactExclusion(-1, "Bob", "+351911111111")
        }
    }

    @Test
    fun `name must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            ContactExclusion(1, "", "+351911111111")
        }
    }

    @Test
    fun `name must not be longer than max name length`() {
        assertFailsWith<IllegalArgumentException> {
            ContactExclusion(
                1,
                "A".repeat(ContactExclusion.MAX_NAME_LENGTH + 1),
                "+351911111111",
            )
        }
    }

    @Test
    fun `phoneNumber must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            ContactExclusion(1, "Bob", "")
        }
    }

    @Test
    fun `phoneNumber must not be longer than max phone number length`() {
        assertFailsWith<IllegalArgumentException> {
            ContactExclusion(
                1,
                "Bob",
                "1".repeat(ContactExclusion.MAX_PHONE_NUMBER_LENGTH + 1),
            )
        }
    }
}
