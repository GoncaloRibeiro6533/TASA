
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.BeforeEach
import pt.isel.Event
import pt.isel.User
import pt.isel.event.MockEventRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MockEventRepositoryTests {
    private val repo = MockEventRepository()

    private val startTime = "2025-10-01T10:00".toLocalDateTime().toJavaLocalDateTime()
    private val endTime = "2025-10-01T11:00".toLocalDateTime().toJavaLocalDateTime()

    private val event =
        Event(
            id = 0,
            title = "Test Event",
            startTime = startTime,
            endTime = endTime,
        )
    private val user =
        User(1, "Bob", "bob@example.com")

    @BeforeEach
    fun setUp() {
        repo.clear()
    }

    @Test
    fun `create should create and return a Event`() {
        val sut = repo.create(event.title, user, event.startTime, event.endTime)
        assert(sut == event)
    }

    @Test
    fun `findById should return the Event with the given id and calendarId`() {
        repo.create(event.title, user, event.startTime, event.endTime)
        val sut = repo.findById(event.id)
        assert(sut == event)
    }

    @Test
    fun `findById should return null if the Event with the given id and calendarId does not exist`() {
        val sut = repo.findById(event.id)
        assert(sut == null)
    }

    @Test
    fun `findAll should return all Events`() {
        val event2 =
            Event(
                id = 2,
                title = "Test Event 2",
                startTime = "2025-10-01T12:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-10-01T13:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        val result = repo.create(event.title, user, event.startTime, event.endTime)
        val result2 = repo.create(event2.title, user, event2.startTime, event2.endTime)
        val sut = repo.findAll()
        assert(sut.size == 2)
        assert(sut.contains(result))
        assert(sut.contains(result2))
    }

    @Test
    fun `findByUserId should return all Events for the given user`() {
        val event2 =
            Event(
                id = 2,
                title = "Test Event 2",
                startTime = "2025-10-01T12:00".toLocalDateTime().toJavaLocalDateTime(),
                endTime = "2025-10-01T13:00".toLocalDateTime().toJavaLocalDateTime(),
            )
        val eventResult = repo.create(event.title, user, event.startTime, event.endTime)
        assertIs<Event>(eventResult)
        val event2Result = repo.create(event2.title, user, event2.startTime, event2.endTime)
        assertIs<Event>(event2Result)
        val sut = repo.findByUserId(user)
        assert(sut.size == 2)
        assert(sut.contains(eventResult))
        assert(sut.contains(event2Result))
    }

    @Test
    fun `update should update the Event and return it`() {
        repo.create(event.title, user, event.startTime, event.endTime)
        val sut = repo.update(user, event, "Updated Event")
        assertEquals(event.id, sut.id)
        assertEquals(event.startTime, sut.startTime)
        assertEquals("Updated Event", sut.title)
    }

    @Test
    fun `delete should remove the Event and return true`() {
        repo.create(event.title, user, event.startTime, event.endTime)
        val sut = repo.delete(user, event)
        assert(sut)
        assert(repo.findById(event.id) == null)
    }
}
