package pt.isel

import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test
import kotlin.test.assertFailsWith

class EventTests {
    private val startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime()
    private val endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime()

    @Test
    fun `event creation succeeds`() {
        val event = Event(1, "Meeting", startTime, endTime)
        assertEquals(1, event.id)
        assertEquals("Meeting", event.title)
        assertEquals(startTime, event.startTime)
        assertEquals(endTime, event.endTime)
    }

    @Test
    fun `event id must be a positive number`() {
        assertFailsWith<IllegalArgumentException> {
            Event(-1, "Meeting", startTime, endTime)
        }
    }

    @Test
    fun `title must not be blank`() {
        assertFailsWith<IllegalArgumentException> {
            Event(1, "", startTime, endTime)
        }
    }

    @Test
    fun `title must not be longer than max title length`() {
        assertFailsWith<IllegalArgumentException> {
            Event(1, "A".repeat(Event.MAX_TITLE_LENGTH + 1), startTime, endTime)
        }
    }

    @Test
    fun `start time must be before end time`() {
        assertFailsWith<IllegalArgumentException> {
            Event(1, "Meeting", endTime, startTime)
        }
    }
}
