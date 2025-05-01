package pt.isel.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.AuthenticatedUser
import pt.isel.Either
import pt.isel.Failure
import pt.isel.Success
import pt.isel.TokenExternalInfo
import pt.isel.User
import pt.isel.UserError
import pt.isel.UserService
import pt.isel.models.Problem
import pt.isel.models.user.LoginOutput
import pt.isel.models.user.UserLoginCredentialsInput
import pt.isel.models.user.UserRegisterInput

/**
 * Controller for managing users.
 * @property userService the user service
 */
@RestController
@RequestMapping("api/user")
class UserController(
    private val userService: UserService,
) {
    /**
     * Registers a new user.
     *
     * @param userRegisterInput the user registration input
     * @return the response entity with the created user
     */
    @PostMapping("/register")
    fun register(
        @RequestBody userRegisterInput: UserRegisterInput,
    ): ResponseEntity<*> {
        val result: Either<UserError, User> =
            userService.register(
                userRegisterInput.username.trim(),
                userRegisterInput.email.trim(),
                userRegisterInput.password,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure ->
                result.value.toResponse()
        }
    }

    /**
     * Logs in a user.
     *
     * @param userLoginCredentialsInput the user login credentials input
     * @return the response entity with the login output
     */
    @PostMapping("/login")
    fun login(
        @RequestBody userLoginCredentialsInput: UserLoginCredentialsInput,
    ): ResponseEntity<*> {
        val result: Either<UserError, Pair<User, TokenExternalInfo>> =
            userService.loginUser(
                userLoginCredentialsInput.username.trim(),
                userLoginCredentialsInput.password,
            )
        return when (result) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(
                    LoginOutput(result.value.first, result.value.second),
                )
            is Failure ->
                result.value.toResponse()
        }
    }

    /**
     * Logs out a user.
     *
     * @param authUser the authenticated user
     * @return the response entity with an empty body
     */
    @PostMapping("/logout")
    fun logout(authUser: AuthenticatedUser): ResponseEntity<*> {
        val result: Either<UserError, Boolean> =
            userService.logoutUser(authUser.token)
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure ->
                result.value.toResponse()
        }
    }

    /**
     * Gets a user by ID.
     *
     * @param id the user ID
     * @param authUser the authenticated user
     * @return the response entity with the user
     */
    @GetMapping("/{id}")
    fun getUser(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        return when (val result: Either<UserError, User> = userService.getUserById(id)) {
            is Success ->
                ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure ->
                result.value.toResponse()
        }
    }

    /**
     * Converts a [UserError] to a [ResponseEntity].
     */
    private fun UserError.toResponse(): ResponseEntity<*> {
        return when (this) {
            is UserError.SessionExpired -> Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED)
            is UserError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
            is UserError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
            is UserError.UsernameAlreadyExists -> Problem.UsernameAlreadyInUse.response(HttpStatus.CONFLICT)
            is UserError.UsernameCannotBeBlank -> Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is UserError.PasswordCannotBeBlank -> Problem.PasswordCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is UserError.EmailCannotBeBlank -> Problem.EmailCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is UserError.InvalidEmail -> Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST)
            is UserError.EmailAlreadyInUse -> Problem.EmailAlreadyInUse.response(HttpStatus.CONFLICT)
            is UserError.UsernameToLong -> Problem.UsernameToLong.response(HttpStatus.BAD_REQUEST)
            is UserError.NoMatchingUsername -> Problem.NoMatchingUsername.response(HttpStatus.NOT_FOUND)
            is UserError.NoMatchingPassword -> Problem.NoMatchingPassword.response(HttpStatus.BAD_REQUEST)
            is UserError.WeakPassword -> Problem.WeakPassword.response(HttpStatus.BAD_REQUEST)
            is UserError.NegativeLimit -> Problem.NegativeLimit.response(HttpStatus.BAD_REQUEST)
            is UserError.NegativeSkip -> Problem.NegativeSkip.response(HttpStatus.BAD_REQUEST)
            is UserError.InvalidTokenFormat -> Problem.InvalidTokenFormat.response(HttpStatus.BAD_REQUEST)
            is UserError.UsernameToShort -> Problem.UsernameTooShort.response(HttpStatus.BAD_REQUEST)
        }
    }
}
