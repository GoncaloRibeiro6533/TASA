package pt.isel

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Duration.Companion.hours

class RuleTests {
    private val clock = TestClock()

    @Test
    fun `rule creation should succeeds`() {
        Rule(1, clock.now(), clock.now() + 1.hours)
    }

    @Test
    fun `rule creation should fail when id is negative`() {
        assertFailsWith<IllegalArgumentException> {
            Rule(-1, clock.now(), clock.now() + 1.hours)
        }
    }

    @Test
    fun `rule creation should fail when endTime is before startTime`() {
        assertFailsWith<IllegalArgumentException> {
            val later = clock.now()
            clock.advance(1.hours)
            Rule(1, clock.now(), later)
        }
    }
}
