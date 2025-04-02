package pt.isel

/**
 * Represents a user.
 * @property id the user's id
 * @property username the user's username
 * @property email the user's email
 * @throws IllegalArgumentException if any of the parameters is invalid
 */

data class User(val id: Int, val username: String, val email: String) {
    init {
        require(id >= 0) { "id must be positive" }
        require(username.isNotBlank()) { "username must not be blank" }
        require(email.isNotBlank()) { "email must not be blank" }
        require(username.length <= MAX_USERNAME_LENGTH) {
            "username must not be longer than $MAX_USERNAME_LENGTH"
        }
        require(email.length <= MAX_EMAIL_LENGTH) {
            "email must not be longer than $MAX_EMAIL_LENGTH"
        }
        require(email.matches("^[a-zA-Z0-9._%+-]+@[a-z.-]+\\.[a-z]{2,4}$".toRegex())) {
            "invalid email"
        }
    }

    companion object {
        const val MAX_USERNAME_LENGTH = 20
        const val MAX_EMAIL_LENGTH = 50
    }
}
