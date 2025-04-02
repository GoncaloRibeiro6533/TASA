package pt.isel

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LocationTests {
    @Test
    fun `location creation succeeds`() {
        val location = Location(1, "ISEL", 38.756387616516704, -9.11648919436834, 2.5)
        assertEquals(1, location.id)
        assertEquals("ISEL", location.name)
        assertEquals(38.756387616516704, location.latitude)
        assertEquals(-9.11648919436834, location.longitude)
    }

    @Test
    fun `location id must be a positive number`() {
        assertFailsWith<IllegalArgumentException> {
            Location(-1, "ISEL", 38.756387616516704, -9.11648919436834, 2.5)
        }
    }

    @Test
    fun `name must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            Location(1, "", 38.756387616516704, -9.11648919436834, 2.5)
        }
    }

    @Test
    fun `name must not be longer than max name length`() {
        assertFailsWith<IllegalArgumentException> {
            Location(
                1,
                "A".repeat(Location.MAX_NAME_LENGTH + 1),
                38.756387616516704,
                -9.11648919436834,
                2.5,
            )
        }
    }

    @Test
    fun `latitude must be between min and max latitude`() {
        assertFailsWith<IllegalArgumentException> {
            Location(1, "ISEL", 90.1, -9.11648919436834, 2.5)
        }
        assertFailsWith<IllegalArgumentException> {
            Location(1, "ISEL", -90.1, -9.11648919436834, 2.5)
        }
    }

    @Test
    fun `longitude must be between min and max longitude`() {
        assertFailsWith<IllegalArgumentException> {
            Location(1, "ISEL", 38.756387616516704, 180.1, 2.5)
        }
        assertFailsWith<IllegalArgumentException> {
            Location(1, "ISEL", 38.756387616516704, -180.1, 2.5)
        }
    }

    @Test
    fun `radius must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            Location(1, "ISEL", 38.756387616516704, -9.11648919436834, -2.5)
        }
    }
}
