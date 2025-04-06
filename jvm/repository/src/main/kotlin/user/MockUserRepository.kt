package user

import pt.isel.User

class MockUserRepository : UserRepository {
    private var currentId = 0
    private val users = mutableMapOf<User, String>()

    override fun findById(id: Int): User? = users.keys.find { it.id == id }

    override fun findByUsername(
        username: String,
        limit: Int,
        skip: Int,
    ): List<User> = users.keys.filter { it.username.uppercase().contains(username.uppercase()) }.drop(skip).take(limit)

    override fun createUser(
        username: String,
        email: String,
        password: String,
    ): User = User(currentId++, username, email).also { users[it] = password }

    override fun updateUsername(
        user: User,
        newUsername: String,
    ): User {
        val password = users.remove(user) ?: throw IllegalArgumentException("User not found")
        val newUser = User(user.id, newUsername, user.email)
        users[newUser] = password
        return newUser
    }

    override fun findByUsernameAndPassword(
        username: String,
        password: String,
    ): User? =
        users[users.keys.find { it.username == username }].let {
            if (it == password) users.keys.find { it.username == username } else null
        }

    override fun delete(user: User) {
        users.remove(user)
    }

    override fun clear() {
        users.clear()
        currentId = 0
    }

    override fun findAll(): List<User> = users.keys.toList()

    override fun findByEmail(email: String): User? = users.keys.find { it.email == email }

    override fun findPasswordOfUser(user: User): String = users[user] ?: throw IllegalArgumentException("User not found")

    override fun findUserMatchesUsername(username: String): User? = users.keys.find { it.username == username }
}
