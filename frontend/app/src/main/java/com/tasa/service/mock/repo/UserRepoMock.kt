package com.tasa.service.mock.repo

import com.tasa.domain.user.User

class UserRepoMock() {
    companion object {
        val users =
            mutableListOf<User>(
                User(1, "Bob", "bob@example.com"),
                User(2, "Alice", "alice@example.com"),
                User(3, "John", "john@example.com"),
            )

        val passwords =
            mutableMapOf(
                1 to "A1234ab",
                2 to "1234VDd",
                3 to "1234SADfs",
            )
        private var currentId = 4
    }

    fun createUser(
        username: String,
        email: String,
        password: String,
    ): User {
        val user = User(currentId++, username, email)
        users.add(user)
        passwords[user.id] = password
        return user
    }

    fun findUserByUsername(
        username: String,
        limit: Int = 10,
        skip: Int = 0,
    ): List<User> {
        return users.filter { it.username.contains(username) }
            .drop(skip)
            .take(limit)
    }

    fun updateUser(
        id: Int,
        newUsername: String,
    ): User {
        val user = users.find { it.id == id }!!
        val newUser = user.copy(username = newUsername)
        users.remove(user)
        users.add(newUser)
        return newUser
    }

    fun findUserById(id: Int): User? {
        return users.find { it.id == id }
    }

    fun findUserByPassword(
        id: Int,
        password: String,
    ): User? {
        return if (passwords[id] == password) users.find { it.id == id } else null
    }

    fun findByEmail(email: String): User? {
        return users.find { it.email == email }
    }
}
