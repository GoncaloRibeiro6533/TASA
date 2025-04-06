
import event.MockEventRepository
import org.junit.jupiter.api.BeforeEach
import pt.isel.Event
import pt.isel.User
import kotlin.test.Test

class MockEventRepositoryTests {
    private val repo = MockEventRepository()

    private val event =
        Event(
            id = 1,
            calendarId = 1,
            title = "Test Event",
        )
    private val user =
        User(1, "Bob", "bob@example.com")

    @BeforeEach
    fun setUp() {
        repo.clear()
    }

    @Test
    fun `create should create and return a Event`() {
        val sut = repo.create(event.id, event.calendarId, event.title, user)
        assert(sut == event)
    }

    @Test
    fun `findById should return the Event with the given id and calendarId`() {
        repo.create(event.id, event.calendarId, event.title, user)
        val sut = repo.findById(event.id, event.calendarId, user)
        assert(sut == event)
    }

    @Test
    fun `findById should return null if the Event with the given id and calendarId does not exist`() {
        val sut = repo.findById(event.id, event.calendarId, user)
        assert(sut == null)
    }

    @Test
    fun `findAll should return all Events`() {
        val event2 =
            Event(
                id = 2,
                calendarId = 1,
                title = "Test Event 2",
            )
        repo.create(event.id, event.calendarId, event.title, user)
        repo.create(event2.id, event2.calendarId, event2.title, user)
        val sut = repo.findAll()
        assert(sut.size == 2)
        assert(sut.contains(event))
        assert(sut.contains(event2))
    }

    @Test
    fun `findByUserId should return all Events for the given user`() {
        val event2 =
            Event(
                id = 2,
                calendarId = 1,
                title = "Test Event 2",
            )
        repo.create(event.id, event.calendarId, event.title, user)
        repo.create(event2.id, event2.calendarId, event2.title, user)
        val sut = repo.findByUserId(user)
        assert(sut.size == 2)
        assert(sut.contains(event))
        assert(sut.contains(event2))
    }

    @Test
    fun `update should update the Event and return it`() {
        val updatedEvent = event.copy(title = "Updated Event")
        repo.create(event.id, event.calendarId, event.title, user)
        val sut = repo.update(updatedEvent, user)
        assert(sut == updatedEvent)
    }

    @Test
    fun `delete should remove the Event and return true`() {
        repo.create(event.id, event.calendarId, event.title, user)
        val sut = repo.delete(event, user)
        assert(sut)
    }
}
