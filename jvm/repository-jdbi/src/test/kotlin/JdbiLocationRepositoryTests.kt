import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.JdbiEventRepository
import pt.isel.JdbiLocationRepository
import pt.isel.JdbiRuleRepository
import pt.isel.JdbiSessionRepository
import pt.isel.JdbiUserRepository
import pt.isel.configureWithAppRequirements
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JdbiLocationRepositoryTests {
    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()
    }

    @BeforeEach
    fun clearDatabase() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).clear()
            JdbiSessionRepository(handle).clear()
            JdbiRuleRepository(handle).clear()
            JdbiEventRepository(handle).clear()
            JdbiLocationRepository(handle).clear()
        }
    }

    @Test
    fun `should create a location`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("user", "user@example.com", "password")
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            assertEquals("Test Location", location.name)
            assertEquals(30.0, location.latitude)
            assertEquals(10.0, location.longitude)
            assertEquals(100.0, location.radius)
        }
    }

    @Test
    fun `should find a location by id`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("user", "user@example.com", "password")
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            val foundLocation = locationRepo.findById(location.id)
            assertNotNull(foundLocation)
            assertEquals(location.id, foundLocation.id)
            assertEquals("Test Location", foundLocation.name)
            assertEquals(30.0, foundLocation.latitude)
            assertEquals(10.0, foundLocation.longitude)
            assertEquals(100.0, foundLocation.radius)
        }
    }

    @Test
    fun `should update a location`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("user", "user@example.com", "password")
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            val updatedLocation = locationRepo.updateName(location, "Updated Location")
            assertNotNull(updatedLocation)
            assertEquals("Updated Location", updatedLocation.name)
            assertEquals(30.0, updatedLocation.latitude)
            assertEquals(10.0, updatedLocation.longitude)
            assertEquals(100.0, updatedLocation.radius)
        }
    }

    @Test
    fun `should delete a location`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("user", "user@example.com", "password")
            val locationRepo = JdbiLocationRepository(handle)
            val location = locationRepo.create("Test Location", 30.0, 10.0, 100.0, user)
            locationRepo.delete(location)
            val foundLocation = locationRepo.findById(location.id)
            assertEquals(null, foundLocation)
        }
    }

    @Test
    fun `should find locations by user`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("user", "user@example.com", "password")
            val locationRepo = JdbiLocationRepository(handle)
            locationRepo.create("Location 1", 30.0, 10.0, 100.0, user)
            locationRepo.create("Location 2", 31.0, 11.0, 200.0, user)
            val locations = locationRepo.findByUserId(user)
            assertEquals(2, locations.size)
            assertTrue(locations.any { it.name == "Location 1" })
            assertTrue(locations.any { it.name == "Location 2" })
        }
    }

    @Test
    fun `should clear repo`() {
        runWithHandle { handle ->
            val userRepo = JdbiUserRepository(handle)
            val user = userRepo.createUser("user1", "user@example.com", "password")
            val locationRepo = JdbiLocationRepository(handle)
            locationRepo.create("Location 1", 30.0, 10.0, 100.0, user)
            locationRepo.clear()
            val locations = locationRepo.findByUserId(user)
            assertEquals(0, locations.size)
        }
    }
}
