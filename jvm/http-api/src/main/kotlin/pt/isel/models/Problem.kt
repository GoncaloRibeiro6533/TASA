package pt.isel.models

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val MEDIA_TYPE = "application/problem+json"
private const val PROBLEM_URI_PATH = ""

sealed class Problem(
    typeUri: URI,
) {
    @Suppress("unused")
    val type = typeUri.toString()
    val title = typeUri.toString().split("/").last()

    fun response(status: HttpStatus): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(this)

    data object InvalidText : Problem(URI("$PROBLEM_URI_PATH/invalid-text"))

    data object NoMatchingUsername : Problem(URI("$PROBLEM_URI_PATH/no-matching-username"))

    data object NoMatchingPassword : Problem(URI("$PROBLEM_URI_PATH/no-matching-password"))

    data object WeakPassword : Problem(URI("$PROBLEM_URI_PATH/weak-password"))

    data object NegativeIdentifier : Problem(URI("$PROBLEM_URI_PATH/negative-identifier"))

    data object EmailAlreadyInUse : Problem(URI("$PROBLEM_URI_PATH/email-already-in-use"))

    data object UserNotFound : Problem(URI("$PROBLEM_URI_PATH/user-not-found"))

    data object UsernameAlreadyInUse : Problem(URI("$PROBLEM_URI_PATH/username-already-in-use"))

    data object UsernameToLong : Problem(URI("$PROBLEM_URI_PATH/username-too-long"))

    data object NegativeSkip : Problem(URI("$PROBLEM_URI_PATH/negative-skip"))

    data object NegativeLimit : Problem(URI("$PROBLEM_URI_PATH/negative-limit"))

    data object UsernameCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/username-cannot-be-blank"))

    data object PasswordCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/password-cannot-be-blank"))

    data object EmailCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/email-cannot-be-blank"))

    data object InvalidEmail : Problem(URI("$PROBLEM_URI_PATH/invalid-email"))

    data object InvalidPassword : Problem(URI("$PROBLEM_URI_PATH/invalid-password"))

    data object SessionExpired : Problem(URI("$PROBLEM_URI_PATH/session-expired"))

    data object Unauthorized : Problem(URI("$PROBLEM_URI_PATH/unauthorized"))

    data object EmailDoesNotMatchInvite : Problem(URI("$PROBLEM_URI_PATH/email-does-not-match-invite"))

    data object InvalidRequestContent : Problem(URI("$PROBLEM_URI_PATH/invalid-request-content"))

    data object InvalidTokenFormat : Problem(URI("$PROBLEM_URI_PATH/invalid-token-format"))

    data object UsernameTooShort : Problem(URI("$PROBLEM_URI_PATH/username-too-short"))
}
