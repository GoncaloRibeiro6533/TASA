package pt.isel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame

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

    @Test
    fun `copy should create a new instance with the same values`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val copy = original.copy()
        assertEquals(1, copy.id)
        assertEquals("Bob", copy.name)
    }

    @Test
    fun `copy should create a new instance with the same values and different id`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val copy = original.copy(id = 2)
        assert(original != copy)
        assert(original.name == copy.name)
        assert(original.phoneNumber == copy.phoneNumber)
    }

    @Test
    fun `copy should create a new instance with the same values and different name`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val copy = original.copy(name = "Alice")
        assert(original != copy)
        assert(original.id == copy.id)
        assert(original.phoneNumber == copy.phoneNumber)
    }

    @Test
    fun `copy should create a new instance with the same values and different phone number`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val copy = original.copy(phoneNumber = "+351922222222")
        assert(original != copy)
        assert(original.id == copy.id)
        assert(original.name == copy.name)
    }

    @Test
    fun `toString should return a string representation of the object`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val expected = "ContactExclusion(id=1, name='Bob', phoneNumber='+351911111111')"
        assert(original.toString() == expected)
    }

    @Test
    fun `equals should return true for the same object`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        assert(original == original)
    }

    @Test
    fun `equals should return false for different id`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val other = ContactExclusion(2, "Bob", "+351911111111")
        assert(original != other)
    }

    @Test
    fun `equals should return false for different name`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val other = ContactExclusion(1, "Alice", "+351911111111")
        assert(original != other)
    }

    @Test
    fun `equals should return false for different phone number`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val other = ContactExclusion(1, "Bob", "+351922222222")
        assert(original != other)
    }

    @Test
    fun `hashCode should return the same hash code for the same object`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        assert(original.hashCode() == original.hashCode())
    }

    @Test
    fun `hashCode should return different hash codes for different objects`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val other = ContactExclusion(2, "Bob", "+351911111111")
        assertNotSame(original.hashCode(), other.hashCode())
    }

    @Test
    fun `toString should return the same string for the same object`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        assert(original.toString() == original.toString())
    }

    @Test
    fun `toString should return different strings for different objects`() {
        val original = ContactExclusion(1, "Bob", "+351911111111")
        val other = ContactExclusion(2, "Alice", "+351922222222")
        assert(original.toString() != other.toString())
    }
}
