import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.JdbiEventRepository
import pt.isel.JdbiLocationRepository
import pt.isel.JdbiRuleRepository
import pt.isel.JdbiSessionRepository
import pt.isel.JdbiUserRepository
import pt.isel.User
import pt.isel.configureWithAppRequirements
import kotlin.jvm.Throws
import kotlin.test.Test
import kotlin.test.assertContains

class JdbiUserRepositoryTests {
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
    fun `should create a user`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")

            assertEquals(user.username, "username")
            assertEquals(user.email, "user@test.com")
        }
    }

    @Test
    fun `when trying to create a user with invalid email, it should not let the action end successfully`() {
        runWithHandle { handle ->
            assertThrows<Exception> {
                JdbiUserRepository(handle).createUser("username", "invalid", "password")
            }
        }
    }

    @Test
    fun `update username should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val updatedUser = JdbiUserRepository(handle).updateUsername(user, "newUsername")

            assertEquals(updatedUser.username, "newUsername")
        }
    }

    @Throws(Exception::class)
    @Test
    fun `update username should return the same user when user does not exist`() {
        runWithHandle { handle ->
            val user = User(99, "username", "user@test.com")
            val updatedUser = JdbiUserRepository(handle).updateUsername(user, "newUsername")
        }
    }

    @Test
    fun `findAll should succeed`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            JdbiUserRepository(handle).createUser("username2", "user2@test.com", "password")
            val users = JdbiUserRepository(handle).findAll()
            assertEquals(users.size, 2)
            assertContains(users.map { it.username }, "username")
            assertContains(users.map { it.username }, "username2")
        }
    }

    @Test
    fun `find by id should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val foundUser = JdbiUserRepository(handle).findById(user.id)

            assertEquals(foundUser?.id, user.id)
        }
    }

    @Test
    fun `find by id should return null when user does not exist`() {
        runWithHandle { handle ->
            val foundUser = JdbiUserRepository(handle).findById(-1)

            assertEquals(foundUser, null)
        }
    }

    @Test
    fun `find by username should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val foundUser = JdbiUserRepository(handle).findByUsername("username", 5, 0)

            assertEquals(foundUser.size, 1)
            assertEquals(foundUser[0].id, user.id)
        }
    }

    @Test
    fun `find by username should return the correct number of users`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            JdbiUserRepository(handle).createUser("username2", "user2@test.com", "password")
            JdbiUserRepository(handle).createUser("zzz", "zzz@test.com", "password")
            val users = JdbiUserRepository(handle).findByUsername("username", 5, 0)
            assertEquals(users.size, 2)
        }
    }

    @Test
    fun `find by username should return the correct number of users when limit is set`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            JdbiUserRepository(handle).createUser("username2", "user2@test.com", "password")
            val users = JdbiUserRepository(handle).findByUsername("username", 1, 0)
            assertEquals(users.size, 1)
        }
    }

    @Test
    fun `find by username should return the correct number of users when skip is set`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            JdbiUserRepository(handle).createUser("username2", "user2@test.com", "password")
            val users = JdbiUserRepository(handle).findByUsername("username", 5, 1)
            assertEquals(users.size, 1)
        }
    }

    @Test
    fun `find by username should return empty list when user does not exist`() {
        runWithHandle { handle ->
            val foundUser = JdbiUserRepository(handle).findByUsername("username", 5, 0)

            assertEquals(foundUser.size, 0)
        }
    }

    @Test
    fun `find by username and password should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val foundUser = JdbiUserRepository(handle).findByUsernameAndPassword("username", "password")
            assertEquals(foundUser?.id, user.id)
        }
    }

    @Test
    fun `find by username and password should return null when user does not exist`() {
        runWithHandle { handle ->
            val foundUser = JdbiUserRepository(handle).findByUsernameAndPassword("username", "password")
            assertEquals(foundUser, null)
        }
    }

    @Test
    fun `find by username and password should return null when password is incorrect`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val foundUser = JdbiUserRepository(handle).findByUsernameAndPassword("username", "incorrect")
            assertEquals(foundUser, null)
        }
    }

    @Test
    fun `find by email should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val foundUser = JdbiUserRepository(handle).findByEmail("user@test.com")
            assertEquals(foundUser?.id, user.id)
        }
    }

    @Test
    fun `find by email should return null when user does not exist`() {
        runWithHandle { handle ->
            val foundUser = JdbiUserRepository(handle).findByEmail("invalidEmail@test.com")
            assertEquals(foundUser, null)
        }
    }

    @Test
    fun `find password of user should succeed`() {
        runWithHandle { handle ->
            val user = JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val password = JdbiUserRepository(handle).findPasswordOfUser(user)
            assertEquals(password, "password")
        }
    }

    @Test
    fun `find password of user should return empty string when user does not exist`() {
        runWithHandle { handle ->
            val user = User(99, "username", "user@test.com")
            val password = JdbiUserRepository(handle).findPasswordOfUser(user)
            assertEquals(password, "")
        }
    }

    @Test
    fun `delete should succeed`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            val user = JdbiUserRepository(handle).findByUsername("username", 5, 0).first()
            JdbiUserRepository(handle).delete(user)
            assertEquals(JdbiUserRepository(handle).findById(user.id), null)
        }
    }

    @Test
    fun `clear should succeed`() {
        runWithHandle { handle ->
            JdbiUserRepository(handle).createUser("username", "user@test.com", "password")
            JdbiUserRepository(handle).createUser("username2", "user2@test.com", "password")
            JdbiUserRepository(handle).clear()
            assertEquals(JdbiUserRepository(handle).findAll().size, 0)
        }
    }
}
