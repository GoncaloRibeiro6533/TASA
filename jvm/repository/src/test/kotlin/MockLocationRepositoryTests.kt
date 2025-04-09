import org.junit.jupiter.api.BeforeEach
import pt.isel.User
import pt.isel.location.MockLocationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MockLocationRepositoryTests {
    private val repo = MockLocationRepository()
    private val user =
        User(
            id = 1,
            username = "Bob",
            email = "bob@example.com",
        )

    @BeforeEach
    fun setUp() {
        repo.clear()
    }

    @Test
    fun `create location should create a location and return it`() {
        val sut =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        assertEquals("ISEL", sut.name)
        assertEquals(38.756387616516704, sut.latitude)
        assertEquals(-9.11648919436834, sut.longitude)
        assertEquals(50.0, sut.radius)
    }

    @Test
    fun `findById should return the location with the given id`() {
        val newLocation =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val sut = repo.findById(newLocation.id)
        assertNotNull(sut)
        assertEquals(newLocation.id, sut.id)
    }

    @Test
    fun `findById should return null if the location with the given id does not exist`() {
        val sut = repo.findById(999)
        assertNull(sut)
    }

    @Test
    fun `findAll should return all locations`() {
        val location1 =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val location2 =
            repo.create(
                "ISEL2",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val sut = repo.findAll()
        assertEquals(2, sut.size)
        assert(sut.contains(location1))
        assert(sut.contains(location2))
    }

    @Test
    fun `findByUserId should return all locations for the given user`() {
        val location1 =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val location2 =
            repo.create(
                "ISEL2",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val sut = repo.findByUserId(user)
        assertEquals(2, sut.size)
        assert(sut.contains(location1))
        assert(sut.contains(location2))
    }

    @Test
    fun `updateRadius should update the radius of the location and return it`() {
        val newLocation =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val updatedLocation = repo.updateRadius(newLocation, 100.0)
        assertEquals(100.0, updatedLocation.radius)
    }

    @Test
    fun `updateName should update the name of the location and return it`() {
        val newLocation =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val updatedLocation = repo.updateName(newLocation, "ISEL Updated")
        assertEquals("ISEL Updated", updatedLocation.name)
    }

    @Test
    fun `updateCoordinates should update the coordinates of the location and return it`() {
        val newLocation =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val updatedLocation = repo.updateCoordinates(newLocation, 39.0, -10.0)
        assertEquals(39.0, updatedLocation.latitude)
        assertEquals(-10.0, updatedLocation.longitude)
    }

    @Test
    fun `delete should remove the location and return true`() {
        val newLocation =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        val sut = repo.delete(newLocation)
        assert(sut)
        assertNull(repo.findById(newLocation.id))
    }

    @Test
    fun `delete should return false if the location does not exist`() {
        val newLocation =
            repo.create(
                "ISEL",
                38.756387616516704,
                -9.11648919436834,
                50.0,
                user,
            )
        repo.delete(newLocation)
        val sut = repo.delete(newLocation)
        assert(!sut)
    }

    @Test
    fun `clear should remove all locations`() {
        repo.create(
            "ISEL",
            38.756387616516704,
            -9.11648919436834,
            50.0,
            user,
        )
        repo.create(
            "ISEL2",
            38.756387616516704,
            -9.11648919436834,
            50.0,
            user,
        )
        repo.clear()
        val sut = repo.findAll()
        assertEquals(0, sut.size)
    }
}
