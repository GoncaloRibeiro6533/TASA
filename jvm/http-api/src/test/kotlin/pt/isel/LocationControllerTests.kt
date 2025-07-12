package pt.isel

import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.controllers.LocationController
import pt.isel.errorHandlers.LocationErrorHandler
import pt.isel.models.location.LocationInput
import pt.isel.models.location.LocationList
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.util.Locale
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class LocationControllerTests {
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
                    refreshTime = 60.days,
                ),
            )

        private fun createLocationErrorHandler(): LocationErrorHandler =
            LocationErrorHandler(
                messageSource = createTestMessageSource(),
            )

        private fun createLocationController(locationService: LocationService) =
            LocationController(locationService, createLocationErrorHandler())
    }

    @BeforeEach
    fun setup() {
        Locale.setDefault(Locale.ENGLISH)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `createLocation should create a new location`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser("Rose Mary", "rose@example.com", passwordValidationInfo.validationInfo)
            }
        val controllerLocations = createLocationController(LocationService(trxManager))
        val locationInput =
            LocationInput(
                name = "Office",
                latitude = 40.7128,
                longitude = -74.0060,
                radius = 100.0,
            )
        val response = controllerLocations.createLocation(AuthenticatedUser(rose, newTokenValidationData()), locationInput)
        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertNotNull(response.body)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getLocation should return the correct location`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser("Rose Mary", "rose@example.com", passwordValidationInfo.validationInfo)
            }
        val location =
            trxManager.run {
                locationRepo.create("Office", 40.7128, -74.0060, 100.0, rose)
            }
        val controllerLocations = createLocationController(LocationService(trxManager))
        val response = controllerLocations.getLocation(AuthenticatedUser(rose, newTokenValidationData()), location.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<Location>(response.body)
        assertEquals("Office", (response.body as Location).name)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `getUserLocations should return a list of locations`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser("Rose Mary", "rose@example.com", passwordValidationInfo.validationInfo)
            }
        trxManager.run {
            locationRepo.create("Office", 40.7128, -74.0060, 100.0, rose)
            locationRepo.create("Home", 41.1496, -8.611, 50.0, rose)
        }
        val controllerLocations = createLocationController(LocationService(trxManager))
        val response = controllerLocations.getUserLocations(AuthenticatedUser(rose, newTokenValidationData()))
        val body = response.body
        assertNotNull(body)
        assertIs<LocationList>(body)
        assertEquals(2, body.nLocations)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateLocationName should update the location name`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser("Rose Mary", "rose@example.com", passwordValidationInfo.validationInfo)
            }
        val location =
            trxManager.run {
                locationRepo.create("Office", 40.7128, -74.0060, 100.0, rose)
            }
        val controllerLocations = createLocationController(LocationService(trxManager))
        val response =
            controllerLocations.updateLocationName(
                AuthenticatedUser(rose, newTokenValidationData()),
                location.id,
                "Updated Office",
            )
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<Location>(response.body)
        assertEquals("Updated Office", (response.body as Location).name)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `updateLocationRadius should update the location radius`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser("Rose Mary", "rose@example.com", passwordValidationInfo.validationInfo)
            }
        val location =
            trxManager.run {
                locationRepo.create("Office", 40.7128, -74.0060, 100.0, rose)
            }
        val controllerLocations = createLocationController(LocationService(trxManager))
        val response = controllerLocations.updateLocationRadius(AuthenticatedUser(rose, newTokenValidationData()), location.id, 200.0)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertIs<Location>(response.body)
        assertEquals(200.0, (response.body as Location).radius)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `deleteLocation should delete the location`(trxManager: TransactionManager) {
        val rose =
            trxManager.run {
                val passwordValidationInfo = usersDomain.createPasswordValidationInformation(newTokenValidationData())
                userRepo.createUser("Rose Mary", "rose@example.com", passwordValidationInfo.validationInfo)
            }
        val location =
            trxManager.run {
                locationRepo.create("Office", 40.7128, -74.0060, 100.0, rose)
            }
        val controllerLocations = createLocationController(LocationService(trxManager))
        val response = controllerLocations.deleteLocation(AuthenticatedUser(rose, newTokenValidationData()), location.id)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNull(response.body)
    }
}
