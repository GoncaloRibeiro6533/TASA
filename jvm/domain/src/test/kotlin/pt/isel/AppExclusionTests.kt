package pt.isel

import kotlin.test.Test
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
}
