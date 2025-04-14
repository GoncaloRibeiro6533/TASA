import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import pt.isel.AppExclusion
import pt.isel.ContactExclusion
import pt.isel.ExclusionError
import pt.isel.ExclusionService
import pt.isel.Failure
import pt.isel.Sha256TokenEncoder
import pt.isel.Success
import pt.isel.User
import pt.isel.UserService
import pt.isel.UsersDomain
import pt.isel.UsersDomainConfig
import pt.isel.transaction.TransactionManager
import pt.isel.transaction.TransactionManagerInMem
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class ExclusionServiceTests {
    companion object {
        @JvmStatic
        fun transactionManagers(): Stream<TransactionManager> =
            Stream.of(
                TransactionManagerInMem().also { cleanup(it) },
                // add JDBI TODO
            )

        private fun cleanup(trxManager: TransactionManager) {
            trxManager.run {
                userRepo.clear()
                sessionRepo.clear()
                ruleRepo.clear()
                exclusionRepo.clear()
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
    fun `create contact exclusion should succeed`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactName = "John Doe"
        val contactPhone = "123456789"
        val sut = exclusionService.createContactExclusion(user.value.id, contactName, contactPhone)
        assertTrue(sut is Success)
        assertIs<ContactExclusion>(sut.value)
        assertEquals(contactName, sut.value.name)
        assertEquals(contactPhone, sut.value.phoneNumber)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create contact exclusion with blank name should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = exclusionService.createContactExclusion(user.value.id, "", "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.BlankContactName>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create contact exclusion with blank phone number should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactName = "John Doe"
        val contactPhone = ""
        val sut = exclusionService.createContactExclusion(user.value.id, contactName, contactPhone)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.BlankPhoneNumber>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create contact exclusion with negative user id should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.createContactExclusion(-1, "John Doe", "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create contact exclusion with contactName too long should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactName = "a".repeat(ContactExclusion.MAX_NAME_LENGTH + 1)
        val sut = exclusionService.createContactExclusion(user.value.id, contactName, "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ContactNameTooLong>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create contact exclusion with phoneNumber too long should return erro`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut =
            exclusionService.createContactExclusion(
                user.value.id,
                "John Doe",
                "123456789" + "1".repeat(ContactExclusion.MAX_PHONE_NUMBER_LENGTH),
            )
        assertTrue(sut is Failure)
        assertIs<ExclusionError.PhoneNumberTooLong>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create contact exclusion with id from a user that does not exists should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.createContactExclusion(99999999, "John Doe", "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create contact exclusion with contact that already exists should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val contactName = "John Doe"
        val contactPhone = "123456789"
        val exclusion = exclusionService.createContactExclusion(user.value.id, contactName, contactPhone)
        assertTrue(exclusion is Success)
        assertIs<ContactExclusion>(exclusion.value)
        val sut = exclusionService.createContactExclusion(user.value.id, contactName, contactPhone)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ExclusionAlreadyExists>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create App exclusion should succeed`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val appName = "Tasa"
        val sut = exclusionService.createAppExclusion(user.value.id, appName)
        assertTrue(sut is Success)
        assertIs<AppExclusion>(sut.value)
        assertEquals(appName, sut.value.name)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create app exclusion called with user id less than 0 should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.createAppExclusion(-1, "Tasa")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create app exclusion called with blank app name should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = exclusionService.createAppExclusion(user.value.id, "")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.AppNameBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create app exclusion called with app name too long should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = exclusionService.createAppExclusion(user.value.id, "Tasa" + "n".repeat(AppExclusion.MAX_NAME_LENGTH))
        assertTrue(sut is Failure)
        assertIs<ExclusionError.AppNameTooLong>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create app exclusion should return error if user does not exists`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.createAppExclusion(1, "Tasa")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `create app should return error if exclusion for given app already exists`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val exclusion = exclusionService.createAppExclusion(user.value.id, "Tasa")
        assertTrue(exclusion is Success)
        assertIs<AppExclusion>(exclusion.value)
        val sut = exclusionService.createAppExclusion(user.value.id, "Tasa")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ExclusionAlreadyExists>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get all exclusions should return empty list if no exclusions exist`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = exclusionService.getUserExclusions(user.value.id)
        assertTrue(sut is Success)
        assertTrue(sut.value.isEmpty())
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get all exclusions should return all existing exclusions of user`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val contactName = "John Doe"
        val contactPhone = "123456789"
        val contactExclusion = exclusionService.createContactExclusion(user.value.id, contactName, contactPhone)
        assertTrue(contactExclusion is Success)
        assertIs<ContactExclusion>(contactExclusion.value)
        val appName = "Tasa"
        val appExclusion = exclusionService.createAppExclusion(user.value.id, appName)
        assertTrue(appExclusion is Success)
        assertIs<AppExclusion>(appExclusion.value)
        val sut = exclusionService.getUserExclusions(user.value.id)
        assertTrue(sut is Success)
        assertEquals(2, sut.value.size)
        assertTrue(sut.value.any { it is ContactExclusion && it.name == contactName && it.phoneNumber == contactPhone })
        assertTrue(sut.value.any { it is AppExclusion && it.name == appName })
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get all exclusions should return error if user does not exists`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.getUserExclusions(1)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `get all exclusions should return error if user id is negative`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.getUserExclusions(-1)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete app exclusion should return error if user id is negative`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.deleteAppExclusion(-1, 1)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete app exclusion should return error if exclusion id is negative`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = exclusionService.deleteAppExclusion(user.value.id, -1)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete app exclusion should return error if user does not exists`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val exclusion = exclusionService.createAppExclusion(user.value.id, "Tasa")
        assertTrue(exclusion is Success)
        assertIs<AppExclusion>(exclusion.value)
        val sut = exclusionService.deleteAppExclusion(user.value.id + 9999, exclusion.value.id)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete app exclusion should return error if app exclusion with given id does not exists`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = exclusionService.deleteAppExclusion(user.value.id, 9999999)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ExclusionNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete app exclusion should succeed`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val exclusion = exclusionService.createAppExclusion(user.value.id, "Tasa")
        assertTrue(exclusion is Success)
        assertIs<AppExclusion>(exclusion.value)
        val sut = exclusionService.deleteAppExclusion(user.value.id, exclusion.value.id)
        assertTrue(sut is Success)
        assertTrue(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete contact exclusion should return error if user id is negative`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.deleteContactExclusion(-1, 1)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete contact exclusion should return error if user does not exists`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val exclusion = exclusionService.createContactExclusion(user.value.id, "John Doe", "123456789")
        assertTrue(exclusion is Success)
        assertIs<ContactExclusion>(exclusion.value)
        val sut = exclusionService.deleteContactExclusion(user.value.id + 9999, exclusion.value.id)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete contact exclusion should return error if exclusion id is negative`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = exclusionService.deleteContactExclusion(user.value.id, -1)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete contact exclusion should return error if contact exclusion does not exists`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val sut = exclusionService.deleteContactExclusion(user.value.id, 9999999)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ExclusionNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `delete contact exclusion should succeeds`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        assertIs<User>(user.value)
        val exclusion = exclusionService.createContactExclusion(user.value.id, "John Doe", "123456789")
        assertTrue(exclusion is Success)
        assertIs<ContactExclusion>(exclusion.value)
        val sut = exclusionService.deleteContactExclusion(user.value.id, exclusion.value.id)
        assertTrue(sut is Success)
        assertTrue(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion should succeed`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactExclusion = exclusionService.createContactExclusion(user.value.id, "John Doe", "123456789")
        assertTrue(contactExclusion is Success)
        val sut =
            exclusionService.updateContactExclusion(user.value.id, contactExclusion.value.id, "Jane Doe", "987654321")
        assertTrue(sut is Success)
        assertEquals("Jane Doe", sut.value.name)
        assertEquals("987654321", sut.value.phoneNumber)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion with blank name should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactExclusion = exclusionService.createContactExclusion(user.value.id, "John Doe", "123456789")
        assertTrue(contactExclusion is Success)
        val sut = exclusionService.updateContactExclusion(user.value.id, contactExclusion.value.id, "", "987654321")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.BlankContactName>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion with blank phone number should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactExclusion = exclusionService.createContactExclusion(user.value.id, "John Doe", "123456789")
        assertTrue(contactExclusion is Success)
        val sut = exclusionService.updateContactExclusion(user.value.id, contactExclusion.value.id, "Jane Doe", "")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.BlankPhoneNumber>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion with contact name too long should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactExclusion = exclusionService.createContactExclusion(user.value.id, "John Doe", "123456789")
        assertTrue(contactExclusion is Success)
        val longName = "a".repeat(ContactExclusion.MAX_NAME_LENGTH + 1)
        val sut =
            exclusionService.updateContactExclusion(user.value.id, contactExclusion.value.id, longName, "987654321")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ContactNameTooLong>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion with phone number too long should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val contactExclusion = exclusionService.createContactExclusion(user.value.id, "John Doe", "123456789")
        assertTrue(contactExclusion is Success)
        val longPhoneNumber = "9".repeat(ContactExclusion.MAX_PHONE_NUMBER_LENGTH + 1)
        val sut =
            exclusionService.updateContactExclusion(
                user.value.id,
                contactExclusion.value.id,
                "Jane Doe",
                longPhoneNumber,
            )
        assertTrue(sut is Failure)
        assertIs<ExclusionError.PhoneNumberTooLong>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion for non-existent user should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.updateContactExclusion(9999999, 1, "John Doe", "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion for non-existent exclusion should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = exclusionService.updateContactExclusion(user.value.id, 9999999, "John Doe", "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ExclusionNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion with negative user id should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.updateContactExclusion(-1, 1, "John Doe", "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update contact exclusion with negative exclusion id should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = exclusionService.updateContactExclusion(user.value.id, -1, "John Doe", "123456789")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update app exclusion should succeed`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val appExclusion = exclusionService.createAppExclusion(user.value.id, "Tasa")
        assertTrue(appExclusion is Success)
        val sut = exclusionService.updateAppExclusion(user.value.id, appExclusion.value.id, "NewAppName")
        assertTrue(sut is Success)
        assertEquals("NewAppName", sut.value.name)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update app exclusion with blank name should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val appExclusion = exclusionService.createAppExclusion(user.value.id, "Tasa")
        assertTrue(appExclusion is Success)
        val sut = exclusionService.updateAppExclusion(user.value.id, appExclusion.value.id, "")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.AppNameBlank>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update app exclusion with name too long should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val appExclusion = exclusionService.createAppExclusion(user.value.id, "Tasa")
        assertTrue(appExclusion is Success)
        val longName = "a".repeat(AppExclusion.MAX_NAME_LENGTH + 1)
        val sut = exclusionService.updateAppExclusion(user.value.id, appExclusion.value.id, longName)
        assertTrue(sut is Failure)
        assertIs<ExclusionError.AppNameTooLong>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update app exclusion for non-existent user should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.updateAppExclusion(9999999, 1, "Tasa")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.UserNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update app exclusion for non-existent exclusion should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = exclusionService.updateAppExclusion(user.value.id, 9999999, "Tasa")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.ExclusionNotFound>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update app exclusion with negative user id should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val sut = exclusionService.updateAppExclusion(-1, 1, "Tasa")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }

    @ParameterizedTest
    @MethodSource("transactionManagers")
    fun `update app exclusion with negative exclusion id should return error`(trxManager: TransactionManager) {
        val exclusionService = ExclusionService(trxManager)
        val userService = createUserService(trxManager, TestClock())
        val user = userService.register("Bob", "bob@example.com", "Tasa_2025")
        assertTrue(user is Success)
        val sut = exclusionService.updateAppExclusion(user.value.id, -1, "Tasa")
        assertTrue(sut is Failure)
        assertIs<ExclusionError.NegativeIdentifier>(sut.value)
    }
}
