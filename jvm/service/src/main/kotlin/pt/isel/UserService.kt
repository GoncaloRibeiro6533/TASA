package pt.isel

import jakarta.inject.Named
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import pt.isel.transaction.TransactionManager

data class TokenExternalInfo(
    val token: String,
    val refreshToken: String,
    val expiration: Instant,
)

/**
 * Error types that can be returned by the [UserService].
 */

sealed class UserError {
    data object UserNotFound : UserError()

    data object UsernameAlreadyExists : UserError()

    data object NoMatchingUsername : UserError()

    data object NoMatchingPassword : UserError()

    data object UsernameToLong : UserError()

    data object UsernameToShort : UserError()

    data object NegativeIdentifier : UserError()

    data object InvalidEmail : UserError()

    data object NegativeSkip : UserError()

    data object NegativeLimit : UserError()

    data object SessionExpired : UserError()

    data object EmailCannotBeBlank : UserError()

    data object UsernameCannotBeBlank : UserError()

    data object PasswordCannotBeBlank : UserError()

    data object EmailAlreadyInUse : UserError()

    data object WeakPassword : UserError()

    data object InvalidTokenFormat : UserError()
}

/**
 * Service for managing users.
 * @property trxManager the transaction manager
 * @property usersDomain the user's domain
 * @property clock the clock used to get the current time
 */
@Named
class UserService(
    private val trxManager: TransactionManager,
    private val usersDomain: UsersDomain,
    private val clock: Clock,
) {
    /**
     * Registers a new user with the given username, email, and password.
     * @param username the username of the user
     * @param email the email of the user
     * @param password the password of the user
     * @return an [Either] containing the created [User] on success or a [UserError] on failure.
     */
    fun register(
        username: String,
        email: String,
        password: String,
    ): Either<UserError, User> =
        trxManager.run {
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (password.isBlank()) return@run failure(UserError.PasswordCannotBeBlank)
            if (email.isBlank()) return@run failure(UserError.EmailCannotBeBlank)
            if (!usersDomain.isValidEmail(email)) return@run failure(UserError.InvalidEmail)
            if (userRepo.findByEmail(email) != null) return@run failure(UserError.EmailAlreadyInUse)
            if (username.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            if (username.length < User.MIN_USERNAME_LENGTH) return@run failure(UserError.UsernameToShort)
            val matches = userRepo.findUserMatchesUsername(username.trim())
            if (matches != null) return@run failure(UserError.UsernameAlreadyExists)
            if (!usersDomain.isPasswordStrong(password)) return@run failure(UserError.WeakPassword)
            val passwordValidationInfo = usersDomain.createPasswordValidationInformation(password)
            val user = userRepo.createUser(username, email, passwordValidationInfo.validationInfo)
            return@run success(user)
        }

    /**
     * Logs in a user with the given username and password, creating a new session with a token and a refresh-token.
     * @param username the username of the user
     * @param password the password of the user
     * @return an [Either] containing [TokenExternalInfo] with the token created on success or a [UserError] on failure.
     */
    fun loginUser(
        username: String,
        password: String,
    ): Either<UserError, Pair<User, TokenExternalInfo>> =
        trxManager.run {
            if (password.isBlank()) return@run failure(UserError.PasswordCannotBeBlank)
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            val user =
                userRepo.findUserMatchesUsername(username.trim()) ?: return@run failure(UserError.NoMatchingUsername)
            val repoPassword = userRepo.findPasswordOfUser(user)
            val passwordValidationInfo = PasswordValidationInfo(repoPassword)
            if (!usersDomain.validatePassword(password, passwordValidationInfo)) {
                return@run failure(UserError.NoMatchingPassword)
            }
            val now = clock.now()
            val token = usersDomain.generateTokenValue()
            val refreshToken = usersDomain.generateTokenValue()
            val newSession =
                sessionRepo.createSession(
                    user,
                    usersDomain.createTokenValidationInformation(token),
                    usersDomain.createTokenValidationInformation(refreshToken),
                    now,
                    now,
                    usersDomain.getSessionExpiration(now, now),
                )
            return@run success(
                user to
                    TokenExternalInfo(
                        token = token,
                        refreshToken = refreshToken,
                        newSession.expirationDate,
                    ),
            )
        }

    /**
     * Logs out a user by deleting their session.
     * @param token the token of the session to be deleted
     * @return an [Either] containing true on success or a [UserError] on failure
     */
    fun logoutUser(token: String) =
        trxManager.run {
            if (token.isBlank()) return@run failure(UserError.InvalidTokenFormat)
            if (!usersDomain.canBeToken(token)) return@run failure(UserError.InvalidTokenFormat)
            val session =
                sessionRepo.findByToken(usersDomain.createTokenValidationInformation(token))
                    ?: return@run failure(UserError.SessionExpired)
            return@run success(sessionRepo.deleteSession(session))
        }

    /**
     * Retrieves a user by their ID.
     * @param id the ID of the user
     * @return an [Either] containing the [User] on success or a [UserError] on failure.
     */
    fun getUserById(id: Int): Either<UserError, User> =
        trxManager.run {
            if (id < 0) return@run failure(UserError.NegativeIdentifier)
            val user = userRepo.findById(id) ?: return@run failure(UserError.UserNotFound)
            return@run success(user)
        }

    /**
     * Retrieves a user by their username.
     * @param username the username of the user
     * @param limit the maximum number of users to return
     * @param skip the number of users to skip
     * @return an [Either] containing a list of [User] on success or a [UserError] on failure.
     */
    fun findUserByUsername(
        username: String,
        limit: Int = 10,
        skip: Int = 0,
    ): Either<UserError, List<User>> =
        trxManager.run {
            if (username.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (limit < 0) return@run failure(UserError.NegativeLimit)
            if (skip < 0) return@run failure(UserError.NegativeSkip)
            val users = userRepo.findByUsername(username, limit, skip)
            return@run success(users)
        }

    /**
     * Updates the username of a user.
     * @param userId the ID of the user
     * @param newUsername the new username of the user
     * @return an [Either] containing the updated [User] on success or a [UserError] on failure.
     */
    fun updateUsername(
        userId: Int,
        newUsername: String,
    ): Either<UserError, User> =
        trxManager.run {
            val user = userRepo.findById(userId) ?: return@run failure(UserError.UserNotFound)
            if (newUsername.isBlank()) return@run failure(UserError.UsernameCannotBeBlank)
            if (newUsername.length < User.MIN_USERNAME_LENGTH) return@run failure(UserError.UsernameToShort)
            if (newUsername.length > User.MAX_USERNAME_LENGTH) return@run failure(UserError.UsernameToLong)
            val matches = userRepo.findUserMatchesUsername(newUsername.trim())
            if (matches != null) return@run failure(UserError.UsernameAlreadyExists)
            val userEdited = userRepo.updateUsername(user, newUsername)
            return@run success(userEdited)
        }

    /**
     * Deletes a user by it's ID.
     * @param userId the ID of the user
     * @return an [Either] containing true on success or a [UserError] on failure.
     */
    fun deleteUser(userId: Int): Either<UserError, Unit> =
        trxManager.run {
            if (userId < 0) return@run failure(UserError.NegativeIdentifier)
            val user = userRepo.findById(userId) ?: return@run failure(UserError.UserNotFound)
            val userDeleted = userRepo.delete(user)
            return@run success(userDeleted)
        }

    /**
     * Retrieves a user by its token.
     * @param token the token of the user session
     * @return the [User] on success or null if the session is expired or the user is not found.
     */
    fun getUserByToken(token: String): User? =
        trxManager.run {
            if (token.isBlank()) return@run null
            if (!usersDomain.canBeToken(token)) return@run null
            val session =
                sessionRepo.findByToken(usersDomain.createTokenValidationInformation(token))
                    ?: return@run null
            if (!usersDomain.isSessionTimeValid(clock, session)) {
                return@run null
            }
            sessionRepo.updateSession(session, clock.now())
            return@run userRepo.findById(session.userId)
        }
}
