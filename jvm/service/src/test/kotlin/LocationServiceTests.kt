import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.Failure
import pt.isel.Location
import pt.isel.LocationError
import pt.isel.LocationService
import pt.isel.Sha256TokenEncoder
import pt.isel.Success
import pt.isel.TransactionManagerJdbi
import pt.isel.User
import pt.isel.UserService
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.configureWithAppRequirements
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class LocationServiceTests {
    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                TransactionManagerJdbi(jdbi).also { cleanup(it) },
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                userRepo.clear()
                sessionRepo.clear()
                ruleRepo.clear()
                eventRepo.clear()
                locationRepo.clear()
            }
        }

        private val usersDomain =
            UsersDomain(
                BCryptPasswordEncoder(),
                Sha256TokenEncoder(),
                UsersDomainConfig(
                    tokenSizeInBytes = 256 / 8,
                    tokenTtl = 30.days,
                    tokenRollingTtl = 30.minutes,
                    maxTokensPerUser = 3,
                ),
            )

        private fun createUserService(
            trxManager: TransactionManager,
            testClock: TestClock,
        ) = UserService(
            trxManager,
            usersDomain,
            testClock,
        )
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create Location should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(sut is Success)
        assertIs<Location>(sut.value)
        assertEquals("ISEL", sut.value.name)
        assertEquals(38.7369, sut.value.latitude)
        assertEquals(-9.1399, sut.value.longitude)
        assertEquals(100.0, sut.value.radius)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create Location should fail with negative userId`(trxManager: TransactionManager) {
        val locationService = LocationService(trxManager)
        val sut =
            locationService.createLocation(
                userId = -1,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create Location should fail with blank name`(trxManager: TransactionManager) {
        val locationService = LocationService(trxManager)
        val sut =
            locationService.createLocation(
                userId = 1,
                name = "",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.InvalidLocationName>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create Location should fail with invalid coordinates`(trxManager: TransactionManager) {
        val locationService = LocationService(trxManager)
        val sut =
            locationService.createLocation(
                userId = 1,
                name = "ISEL",
                latitude = 100.0,
                longitude = -200.0,
                radius = 100.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.InvalidLocationCoordinates>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create Location should return error if user not found`(trxManager: TransactionManager) {
        val locationService = LocationService(trxManager)
        val sut =
            locationService.createLocation(
                userId = 999,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create Location should return error if location already exists with given name`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 40.7369,
                longitude = -9.1399,
                radius = 50.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.AlreadyExists>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create Location should fail with negative radius`(trxManager: TransactionManager) {
        val locationService = LocationService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = -100.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.InvalidLocationRadius>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get Location by its id should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.getLocationById(
                userId = user.value.id,
                id = location0.value.id,
            )
        assertTrue(sut is Success)
        assertIs<Location>(sut.value)
        assertEquals("ISEL", sut.value.name)
        assertEquals(38.7369, sut.value.latitude)
        assertEquals(-9.1399, sut.value.longitude)
        assertEquals(100.0, sut.value.radius)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get Location by its id should fail with negative userId`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.getLocationById(
                userId = -1,
                id = location0.value.id,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get location by its id should return error if location id is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            locationService.getLocationById(
                userId = user.value.id,
                id = -1,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get location by its id should return error if user does nto exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.getLocationById(
                userId = 999,
                id = 1,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get location by its id should return error if location with given id does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            locationService.getLocationById(
                userId = user.value.id,
                id = 999,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.LocationNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get location by its id should return error if location does not belong to user`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val user1 = userService.register("Alice", "alice@example.com", "Tasa_2025")
        assertTrue(user1 is Success)
        assertIs<User>(user1.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.getLocationById(
                userId = user1.value.id,
                id = location0.value.id,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NotAllowed>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get all locations should return all locations of user`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val location1 =
            locationService.createLocation(
                userId = user.value.id,
                name = "IPL",
                latitude = 38.7494,
                longitude = -9.1963,
                radius = 100.0,
            )
        assertTrue(location1 is Success)
        assertIs<Location>(location1.value)
        val sut =
            locationService.getAllLocations(
                userId = user.value.id,
            )
        assertTrue(sut is Success)
        assertTrue(sut.value.contains(location0.value))
        assertTrue(sut.value.contains(location1.value))
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get all locations should return error of user id is negative`(trxManager: TransactionManager) {
        val locationService = LocationService(trxManager)
        val sut = locationService.getAllLocations(-1)
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get all locations should return error of user does not exists`(trxManager: TransactionManager) {
        val locationService = LocationService(trxManager)
        val sut = locationService.getAllLocations(999999)
        assertTrue(sut is Failure)
        assertIs<LocationError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location name should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationName(
                userId = user.value.id,
                locationId = location0.value.id,
                name = "IPL",
            )
        assertTrue(sut is Success)
        assertIs<Location>(sut.value)
        assertEquals("IPL", sut.value.name)
        assertEquals(38.7369, sut.value.latitude)
        assertEquals(-9.1399, sut.value.longitude)
        assertEquals(100.0, sut.value.radius)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location name should fail with negative userId`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationName(
                userId = -1,
                locationId = location0.value.id,
                name = "IPL",
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location name should fail with negative locationId`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationName(
                userId = user.value.id,
                locationId = -1,
                name = "IPL",
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location name should return error with blank name`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationName(
                userId = user.value.id,
                locationId = location0.value.id,
                name = "",
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.InvalidLocationName>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location name should return error if location with given name already exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val location1 =
            locationService.createLocation(
                userId = user.value.id,
                name = "IPL",
                latitude = 38.7494,
                longitude = -9.1963,
                radius = 100.0,
            )
        assertTrue(location1 is Success)
        assertIs<Location>(location1.value)
        val sut =
            locationService.updateLocationName(
                userId = user.value.id,
                locationId = location0.value.id,
                name = "IPL",
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.AlreadyExists>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location name should return error if user with the given id does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationName(
                userId = 999,
                locationId = location0.value.id,
                name = "IPL",
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location name should return error if location with given id does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            locationService.updateLocationName(
                userId = user.value.id,
                locationId = 999,
                name = "IPL",
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.LocationNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location radius should succeed`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationRadius(
                userId = user.value.id,
                locationId = location0.value.id,
                radius = 200.0,
            )
        assertTrue(sut is Success)
        assertIs<Location>(sut.value)
        assertEquals(200.0, sut.value.radius)
        assertEquals("ISEL", sut.value.name)
        assertEquals(38.7369, sut.value.latitude)
        assertEquals(-9.1399, sut.value.longitude)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location radius should return error if user id is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationRadius(
                userId = -1,
                locationId = location0.value.id,
                radius = 200.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location radius should return error if location id is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            locationService.updateLocationRadius(
                user.value.id,
                -1,
                100.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location radius should return error if radius is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationRadius(
                userId = user.value.id,
                locationId = location0.value.id,
                radius = -200.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.InvalidLocationRadius>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location radius should return error if user with given id does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationRadius(
                userId = 9999,
                locationId = location0.value.id,
                radius = 200.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location radius should return error if location does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            locationService.updateLocationRadius(
                userId = user.value.id,
                locationId = 999999,
                radius = 200.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.LocationNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update location radius should return error if location does not belong to user`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val user1 = userService.register("Alice", "alice@example.com", "Tasa_2025")
        assertTrue(user1 is Success)
        assertIs<User>(user1.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.updateLocationRadius(
                userId = user1.value.id,
                locationId = location0.value.id,
                radius = 200.0,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NotAllowed>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete location should delete the location with the given id`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.deleteLocation(
                user.value.id,
                location0.value.id,
            )
        assertTrue(sut is Success)
        assertTrue(sut.value)
        val sut1 = locationService.getLocationById(user.value.id, location0.value.id)
        assertTrue(sut1 is Failure)
        assertIs<LocationError.LocationNotFound>(sut1.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete location should return error if user id is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.deleteLocation(
                userId = -1,
                locationId = location0.value.id,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete location should return error if location id is negative`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.deleteLocation(
                userId = user.value.id,
                locationId = -1,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete location should return error if user with given id is not found`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.deleteLocation(
                userId = 99999,
                locationId = location0.value.id,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete location should return error if location with given id is does not exists`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.deleteLocation(
                userId = user.value.id,
                locationId = 999999,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.LocationNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete location should return error if location does not belong to user`(trxManager: TransactionManager) {
        val userService = createUserService(trxManager, TestClock())
        val locationService = LocationService(trxManager)
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val user1 = userService.register("Alice", "alice@example.com", "Tasa_2025")
        assertTrue(user1 is Success)
        assertIs<User>(user1.value)
        val location0 =
            locationService.createLocation(
                userId = user.value.id,
                name = "ISEL",
                latitude = 38.7369,
                longitude = -9.1399,
                radius = 100.0,
            )
        assertTrue(location0 is Success)
        assertIs<Location>(location0.value)
        val sut =
            locationService.deleteLocation(
                userId = user1.value.id,
                locationId = location0.value.id,
            )
        assertTrue(sut is Failure)
        assertIs<LocationError.NotAllowed>(sut.value)
    }
}
