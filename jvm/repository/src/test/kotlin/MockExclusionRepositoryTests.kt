import org.junit.jupiter.api.BeforeEach
import pt.isel.AppExclusion
import pt.isel.ContactExclusion
import pt.isel.User
import pt.isel.exclusion.MockExclusionRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class MockExclusionRepositoryTests {
    private val repo = MockExclusionRepository()
    private val appExclusion =
        AppExclusion(
            id = 1,
            name = "Test App",
        )
    private val contactExclusion =
        ContactExclusion(
            id = 1,
            name = "Test Contact",
            phoneNumber = "123456789",
        )
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
    fun `createContactExclusion should create a ContactExclusion and return it`() {
        val sut =
            repo.createContactExclusion(
                contactName = contactExclusion.name,
                phoneNumber = contactExclusion.phoneNumber,
                user = user,
            )
        assertEquals(contactExclusion.name, sut.name)
        assertEquals(contactExclusion.phoneNumber, sut.phoneNumber)
    }

    @Test
    fun `createAppExclusion should create a AppExclusion and return it`() {
        val sut =
            repo.createAppExclusion(
                appName = appExclusion.name,
                user = user,
            )
        assertEquals(appExclusion.name, sut.name)
    }

    @Test
    fun `findAll should return all exclusions`() {
        repo.createAppExclusion(
            appName = appExclusion.name,
            user = user,
        )
        repo.createContactExclusion(
            contactName = contactExclusion.name,
            phoneNumber = contactExclusion.phoneNumber,
            user = user,
        )
        val sut = repo.findAll()
        assertEquals(2, sut.size)
    }

    @Test
    fun `findAllAppExclusions should return all AppExclusions`() {
        val newExclusion =
            repo.createAppExclusion(
                appName = appExclusion.name,
                user = user,
            )
        val sut = repo.findAllAppExclusions()
        assertEquals(1, sut.size)
        assertEquals(newExclusion.id, sut[0].id)
    }

    @Test
    fun `findAllContactExclusions should return all ContactExclusions`() {
        val newExclusion =
            repo.createContactExclusion(
                contactName = contactExclusion.name,
                phoneNumber = contactExclusion.phoneNumber,
                user = user,
            )
        val sut = repo.findAllContactExclusions()
        assertEquals(1, sut.size)
        assertEquals(newExclusion.id, sut[0].id)
    }

    @Test
    fun `findByIdAppExclusions should return the AppExclusion with the given id`() {
        val newExclusion =
            repo.createAppExclusion(
                appName = appExclusion.name,
                user = user,
            )
        val sut = repo.findByIdAppExclusions(newExclusion.id)
        assertEquals(newExclusion.id, sut?.id)
    }

    @Test
    fun `findByIdContactExclusions should return the ContactExclusion with the given id`() {
        val newExclusion =
            repo.createContactExclusion(
                contactName = contactExclusion.name,
                phoneNumber = contactExclusion.phoneNumber,
                user = user,
            )
        val sut = repo.findByIdContactExclusions(newExclusion.id)
        assertEquals(newExclusion.id, sut?.id)
    }

    @Test
    fun `findAppExclusionsByUserId should return the AppExclusions for the given user id`() {
        val newExclusion =
            repo.createAppExclusion(
                appName = appExclusion.name,
                user = user,
            )
        val sut = repo.findAppExclusionsByUserId(user)
        assertEquals(1, sut.size)
        assertEquals(newExclusion.id, sut[0].id)
    }

    @Test
    fun `findContactExclusionsByUserId should return the ContactExclusions for the given user id`() {
        val newExclusion =
            repo.createContactExclusion(
                contactName = contactExclusion.name,
                phoneNumber = contactExclusion.phoneNumber,
                user = user,
            )
        val sut = repo.findContactExclusionsByUserId(user)
        assertEquals(1, sut.size)
        assertEquals(newExclusion.id, sut[0].id)
    }

    @Test
    fun `findAllExclusionsByUserId should return all Exclusions for the given user id`() {
        val newAppExclusion =
            repo.createAppExclusion(
                appName = appExclusion.name,
                user = user,
            )
        val newContactExclusion =
            repo.createContactExclusion(
                contactName = contactExclusion.name,
                phoneNumber = contactExclusion.phoneNumber,
                user = user,
            )
        val sut = repo.findAllExclusionsByUserId(user)
        assertEquals(2, sut.size)
        assertEquals(newAppExclusion.id, sut[0].id)
        assertEquals(newContactExclusion.id, sut[1].id)
    }

    @Test
    fun `updateAppExclusion should update the AppExclusion and return it`() {
        val newExclusion =
            repo.createAppExclusion(
                appName = appExclusion.name,
                user = user,
            )
        val updatedExclusion =
            repo.updateAppExclusion(
                appExclusion = newExclusion,
                appName = "Updated App",
            )
        assertEquals("Updated App", updatedExclusion.name)
    }

    @Test
    fun `updateContactExclusion should update the ContactExclusion and return it`() {
        val newExclusion =
            repo.createContactExclusion(
                contactName = contactExclusion.name,
                phoneNumber = contactExclusion.phoneNumber,
                user = user,
            )
        val updatedExclusion =
            repo.updateContactExclusion(
                contactExclusion = newExclusion,
                contactName = "Updated Contact",
                phoneNumber = "987654321",
            )
        assertEquals("Updated Contact", updatedExclusion.name)
        assertEquals("987654321", updatedExclusion.phoneNumber)
    }

    @Test
    fun `deleteAppExclusion should delete the AppExclusion and return true`() {
        val newExclusion =
            repo.createAppExclusion(
                appName = appExclusion.name,
                user = user,
            )
        val result = repo.deleteAppExclusion(newExclusion)
        assertEquals(true, result)
    }

    @Test
    fun `deleteContactExclusion should delete the ContactExclusion and return true`() {
        val newExclusion =
            repo.createContactExclusion(
                contactName = contactExclusion.name,
                phoneNumber = contactExclusion.phoneNumber,
                user = user,
            )
        val result = repo.deleteContactExclusion(newExclusion)
        assertEquals(true, result)
    }

    @Test
    fun `clear should remove all exclusions`() {
        repo.createAppExclusion(
            appName = appExclusion.name,
            user = user,
        )
        repo.createContactExclusion(
            contactName = contactExclusion.name,
            phoneNumber = contactExclusion.phoneNumber,
            user = user,
        )
        repo.clear()
        val sut = repo.findAll()
        assertEquals(0, sut.size)
    }
}
