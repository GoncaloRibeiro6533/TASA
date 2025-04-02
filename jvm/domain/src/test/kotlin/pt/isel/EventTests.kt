package pt.isel

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith

class EventTests {
    @Test
    fun `event creation succeeds`() {
        val event = Event(1, 1, "Meeting")
        assertEquals(1, event.id)
        assertEquals(1, event.calendarId)
        assertEquals("Meeting", event.title)
    }

    @Test
    fun `event id must be a positive number`() {
        assertFailsWith<IllegalArgumentException> {
            Event(-1, 1, "Meeting")
        }
    }

    @Test
    fun `calendarId must be a positive number`() {
        assertFailsWith<IllegalArgumentException> {
            Event(1, -1, "Meeting")
        }
    }

    @Test
    fun `title must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            Event(1, 1, "")
        }
    }

    @Test
    fun `title must not be longer than max title length`() {
        assertFailsWith<IllegalArgumentException> {
            Event(1, 1, "A".repeat(Event.MAX_TITLE_LENGTH + 1))
        }
    }
}
