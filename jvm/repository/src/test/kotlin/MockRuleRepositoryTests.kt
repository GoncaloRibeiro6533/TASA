import kotlinx.datetime.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import pt.isel.Event
import pt.isel.Location
import pt.isel.Rule
import pt.isel.User
import pt.isel.rule.MockRuleRepository
import kotlin.test.assertEquals

class MockRuleRepositoryTests {
    private val repo = MockRuleRepository()
    private val user =
        User(
            id = 1,
            username = "Bob",
            email = "bob@example.pt",
        )
    private val event =
        Event(
            id = 1,
            calendarId = 1,
            title = "Test Event",
        )
    private val location =
        Location(
            id = 1,
            name = "Test Location",
            latitude = 38.756387616516704,
            longitude = -9.11648919436834,
            radius = 50.0,
        )

    @BeforeEach
    fun setUp() {
        repo.clear()
    }

    @Test
    fun `createEventRule should create an event rule and return it`() {
        val sut =
            repo.createEventRule(
                event = event,
                user = user,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val expectedRule =
            Rule(
                id = 0,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        assertEquals(expectedRule.startTime, sut.startTime)
        assertEquals(expectedRule.endTime, sut.endTime)
        assertEquals(expectedRule.id, sut.id)
    }

    @Test
    fun `createLocationRule should create a location rule and return it`() {
        val sut =
            repo.createLocationRule(
                locationId = location.id,
                user = user,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val expectedRule =
            Rule(
                id = 0,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        assertEquals(expectedRule.startTime, sut.startTime)
        assertEquals(expectedRule.endTime, sut.endTime)
        assertEquals(expectedRule.id, sut.id)
    }

    @Test
    fun `findAll should return all rules`() {
        val rule1 =
            repo.createEventRule(
                event = event,
                user = user,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val rule2 =
            repo.createLocationRule(
                locationId = location.id,
                user = user,
                startTime = Instant.parse("2025-06-01T02:00:00Z"),
                endTime = Instant.parse("2025-06-01T03:00:00Z"),
            )
        val sut = repo.findAll()
        assertEquals(2, sut.size)
        assertEquals(rule1, sut[0])
        assertEquals(rule2, sut[1])
    }

    @Test
    fun `findById should return the rule with the given id`() {
        val rule =
            repo.createEventRule(
                event = event,
                user = user,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val sut = repo.findById(rule.id)
        assertEquals(rule, sut)
    }

    @Test
    fun `findById should return null if the rule with the given id does not exist`() {
        val sut = repo.findById(999)
        assertEquals(null, sut)
    }

    @Test
    fun `findByUserId should return all rules for the given user`() {
        val rule1 =
            repo.createEventRule(
                event = event,
                user = user,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val rule2 =
            repo.createLocationRule(
                locationId = location.id,
                user = user,
                startTime = Instant.parse("2025-06-01T02:00:00Z"),
                endTime = Instant.parse("2025-06-01T03:00:00Z"),
            )
        val sut = repo.findByUserId(user)
        assertEquals(2, sut.size)
        assertEquals(rule1, sut[0])
        assertEquals(rule2, sut[1])
    }

    @Test
    fun `update should update the rule and return it`() {
        val rule =
            repo.createEventRule(
                event = event,
                user = user,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val sut =
            repo.update(
                rule = rule,
                startTime = Instant.parse("2025-06-01T01:00:00Z"),
                endTime = Instant.parse("2025-06-01T02:00:00Z"),
            )
        assertEquals(Instant.parse("2025-06-01T01:00:00Z"), sut.startTime)
        assertEquals(Instant.parse("2025-06-01T02:00:00Z"), sut.endTime)
    }

    @Test
    fun `delete should remove the rule and return true`() {
        val rule =
            repo.createEventRule(
                event = event,
                user = user,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val sut = repo.delete(rule)
        assertEquals(true, sut)
        assertEquals(null, repo.findById(rule.id))
    }

    @Test
    fun `delete should return false if the rule does not exist`() {
        val rule =
            Rule(
                id = 999,
                startTime = Instant.parse("2025-06-01T00:00:00Z"),
                endTime = Instant.parse("2025-06-01T01:00:00Z"),
            )
        val sut = repo.delete(rule)
        assertEquals(false, sut)
    }

    @Test
    fun `clear should remove all rules`() {
        repo.createEventRule(
            event = event,
            user = user,
            startTime = Instant.parse("2025-06-01T00:00:00Z"),
            endTime = Instant.parse("2025-06-01T01:00:00Z"),
        )
        repo.createLocationRule(
            locationId = location.id,
            user = user,
            startTime = Instant.parse("2025-06-01T02:00:00Z"),
            endTime = Instant.parse("2025-06-01T03:00:00Z"),
        )
        repo.clear()
        assertEquals(0, repo.findAll().size)
    }

    @Test
    fun `findByUserId should return an empty list if the user has no rules`() {
        val sut = repo.findByUserId(user)
        assertEquals(0, sut.size)
    }
}
