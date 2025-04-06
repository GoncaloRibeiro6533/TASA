import org.junit.jupiter.api.BeforeEach
import pt.isel.User
import user.MockUserRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MockUserRepositoryTests {
    private val user = User(1, "Bob", "bob@example.com")
    private val password = "password"
    private val repo = MockUserRepository()

    @BeforeEach
    fun setUp() {
        repo.clear()
    }

    @Test
    fun `createUser should return a user`() {
        val sut = repo.createUser(user.username, user.email, password)
        assertNotNull(sut)
        assertEquals(user.username, sut.username)
        assertEquals(user.email, sut.email)
    }

    @Test
    fun `findById should return a user`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val sut = repo.findById(newUser.id)
        assertNotNull(sut)
        assertEquals(newUser.id, sut.id)
        assertEquals(newUser.username, sut.username)
        assertEquals(newUser.email, sut.email)
    }

    @Test
    fun `calling clear should clear the repository`() {
        val newUser = repo.createUser(user.username, user.email, password)
        assertEquals(newUser, repo.findById(newUser.id))
        repo.clear()
        val sut = repo.findAll()
        assertEquals(0, sut.size)
    }

    @Test
    fun `findAll should return all users`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val anotherUser = repo.createUser("Alice", "alice@example.com", "password")
        val sut = repo.findAll()
        assertEquals(2, sut.size)
        assertEquals(newUser.username, sut[0].username)
        assertEquals(newUser.email, sut[0].email)
        assertEquals(anotherUser.username, sut[1].username)
        assertEquals(anotherUser.email, sut[1].email)
    }

    @Test
    fun `findByUsername should return a list of users`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val anotherUser = repo.createUser("BobCat", "bobcat@example.com", "password")
        val sut = repo.findByUsername("bob", 2, 0)
        assertEquals(2, sut.size)
        assertEquals(newUser.username, sut[0].username)
        assertEquals(newUser.email, sut[0].email)
        assertEquals(anotherUser.username, sut[1].username)
        assertEquals(anotherUser.email, sut[1].email)
        val sut2 = repo.findByUsername("b", 1, 0)
        assertEquals(1, sut2.size)
    }

    @Test
    fun `updateUsername should update user username`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val updatedUser = repo.updateUsername(newUser, "Alice")
        assertEquals("Alice", updatedUser.username)
        assertEquals(newUser.email, updatedUser.email)
        val sut = repo.findById(newUser.id)
        assertEquals("Alice", sut?.username)
    }

    @Test
    fun `findByUsernameAndPassword should return a user`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val sut = repo.findByUsernameAndPassword(newUser.username, password)
        assertNotNull(sut)
        assertEquals(newUser.id, sut.id)
    }

    @Test
    fun `delete should remove user`() {
        val newUser = repo.createUser(user.username, user.email, password)
        assertEquals(newUser, repo.findById(newUser.id))
        repo.delete(newUser)
        val sut = repo.findById(newUser.id)
        assertEquals(null, sut)
    }

    @Test
    fun `findByEmail should return a user`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val sut = repo.findByEmail(newUser.email)
        assertNotNull(sut)
        assertEquals(newUser.id, sut.id)
    }

    @Test
    fun `findPasswordOfUser should return a password`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val sut = repo.findPasswordOfUser(newUser)
        assertEquals(password, sut)
    }

    @Test
    fun `findUserMatchesUsername should return a user`() {
        val newUser = repo.createUser(user.username, user.email, password)
        val sut = repo.findUserMatchesUsername(newUser.username)
        assertNotNull(sut)
        assertEquals(newUser.id, sut.id)
    }
}
