package pt.isel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AppExclusionTests {
    @Test
    fun `app exclusion succeeds`() {
        AppExclusion(1, "App")
    }

    @Test
    fun `id must be a positive number`() {
        assertFailsWith<IllegalArgumentException> {
            AppExclusion(-1, "App")
        }
    }

    @Test
    fun `name must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            AppExclusion(1, "")
        }
    }

    @Test
    fun `name must not be longer than max name length`() {
        assertFailsWith<IllegalArgumentException> {
            AppExclusion(1, "A".repeat(AppExclusion.MAX_NAME_LENGTH + 1))
        }
    }

    @Test
    fun `copy should create a new instance with the same values`() {
        val original = AppExclusion(1, "App")
        val copy = original.copy()
        assertEquals(1, copy.id)
        assertEquals("App", copy.name)
    }

    @Test
    fun `copy should create a new instance with the same values and different id`() {
        val original = AppExclusion(1, "App")
        val copy = original.copy(id = 2)
        assert(original != copy)
        assert(original.name == copy.name)
    }

    @Test
    fun `copy should create a new instance with the same values and different name`() {
        val original = AppExclusion(1, "App")
        val copy = original.copy(name = "New App")
        assert(original != copy)
        assert(original.id == copy.id)
    }

    @Test
    fun `toString should return a string representation of the object`() {
        val original = AppExclusion(1, "App")
        val expected = "AppExclusion(id=1, name='App')"
        assert(original.toString() == expected)
    }

    @Test
    fun `equals should return true for the same object`() {
        val original = AppExclusion(1, "App")
        assert(original == original)
    }

    @Test
    fun `equals should return false for different id`() {
        val original = AppExclusion(1, "App")
        val other = AppExclusion(2, "App")
        assert(original != other)
    }

    @Test
    fun `equals should return false for different name`() {
        val original = AppExclusion(1, "App")
        val other = AppExclusion(1, "New App")
        assert(original != other)
    }

    @Test
    fun `hashCode should return the same hash code for the same object`() {
        val original = AppExclusion(1, "App")
        assert(original.hashCode() == original.hashCode())
    }
}
