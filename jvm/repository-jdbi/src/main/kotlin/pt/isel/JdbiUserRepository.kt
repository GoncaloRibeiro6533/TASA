package pt.isel

import org.jdbi.v3.core.Handle
import pt.isel.user.UserRepository

class JdbiUserRepository(
    private val handle: Handle,
) : UserRepository {
    override fun findById(id: Int): User? {
        return handle.createQuery("SELECT id, email, username FROM ps.User WHERE id = :id")
            .bind("id", id)
            .mapTo(User::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findByUsername(
        username: String,
        limit: Int,
        skip: Int,
    ): List<User> {
        return handle.createQuery(
            "SELECT * FROM ps.User WHERE UPPER(username) LIKE UPPER(:username) || '%' ORDER BY username LIMIT :limit OFFSET :skip",
        )
            .bind("username", username)
            .bind("limit", limit)
            .bind("skip", skip)
            .mapTo(User::class.java)
            .list()
    }

    override fun createUser(
        username: String,
        email: String,
        password: String,
    ): User {
        return handle.createUpdate("INSERT INTO ps.User (username, email, passwordhash) VALUES (:username, :email, :password)")
            .bind("username", username)
            .bind("email", email)
            .bind("password", password)
            .executeAndReturnGeneratedKeys()
            .mapTo(User::class.java)
            .one()
    }

    override fun updateUsername(
        user: User,
        newUsername: String,
    ): User {
        handle.createUpdate(
            """
            UPDATE ps.User set username = :newUsername WHERE id = :id
            """.trimIndent(),
        ).bind("newUsername", newUsername)
            .bind("id", user.id)
            .execute()
        return user.copy(username = newUsername)
    }

    override fun findByUsernameAndPassword(
        username: String,
        password: String,
    ): User? {
        return handle.createQuery("SELECT * FROM ps.User WHERE username = :username AND passwordhash = :password")
            .bind("username", username)
            .bind("password", password)
            .mapTo(User::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun delete(user: User) {
        handle.createUpdate("DELETE FROM ps.User WHERE id = :id")
            .bind("id", user.id)
            .execute()
    }

    override fun clear() {
        handle.createUpdate("DELETE FROM ps.User")
            .execute()
    }

    override fun findAll(): List<User> {
        return handle.createQuery("SELECT * FROM ps.User")
            .mapTo(User::class.java)
            .list()
    }

    override fun findByEmail(email: String): User? {
        return handle.createQuery("SELECT * FROM ps.User WHERE email = :email")
            .bind("email", email)
            .mapTo(User::class.java)
            .findFirst()
            .orElse(null)
    }

    override fun findPasswordOfUser(user: User): String {
        return handle.createQuery("SELECT passwordhash FROM ps.User WHERE id = :id")
            .bind("id", user.id)
            .mapTo(String::class.java)
            .findOne()
            .orElse("")
    }

    override fun findUserMatchesUsername(username: String): User? {
        return handle.createQuery("SELECT * FROM ps.User WHERE username = :username")
            .bind("username", username)
            .mapTo(User::class.java)
            .findFirst()
            .orElse(null)
    }
}
