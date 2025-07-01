package pt.isel.models

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val MEDIA_TYPE = "application/problem+json"
private const val PROBLEM_URI_PATH = "https://github.com/GoncaloRibeiro6533/TASA/tree/main/docs/problems"

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

    data object NotAllowed : Problem(URI("$PROBLEM_URI_PATH/not-allowed"))

    data object InvalidCoordinate : Problem(URI("$PROBLEM_URI_PATH/invalid-coordinate"))

    data object InvalidLatitude : Problem(URI("$PROBLEM_URI_PATH/invalid-latitude"))

    data object InvalidLongitude : Problem(URI("$PROBLEM_URI_PATH/invalid-longitude"))

    data object InvalidRadius : Problem(URI("$PROBLEM_URI_PATH/invalid-radius"))

    data object EndTimeMustBeBeforeStartTime : Problem(URI("$PROBLEM_URI_PATH/end-time-must-be-before-start-time"))

    data object StartTimeMustBeBeforeEndTime : Problem(URI("$PROBLEM_URI_PATH/start-time-must-be-before-end-time"))

    data object RuleNotFound : Problem(URI("$PROBLEM_URI_PATH/rule-not-found"))

    data object TitleCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/title-cannot-be-blank"))

    data object RuleAlreadyExistsForGivenTime : Problem(URI("$PROBLEM_URI_PATH/rule-already-exists-for-given-time"))

    data object ContactNameCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/contact-name-cannot-be-blank"))

    data object ContactPhoneNumberCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/contact-phone-number-cannot-be-blank"))

    data object ContactNameTooLong : Problem(URI("$PROBLEM_URI_PATH/contact-name-too-long"))

    data object PhoneNumberTooLong : Problem(URI("$PROBLEM_URI_PATH/phone-number-too-long"))

    data object AppNameCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/app-name-cannot-be-blank"))

    data object AppNameTooLong : Problem(URI("$PROBLEM_URI_PATH/app-name-too-long"))

    data object ExclusionAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/exclusion-already-exists"))

    data object ExclusionNotFound : Problem(URI("$PROBLEM_URI_PATH/exclusion-not-found"))

    data object InvalidExclusionIdentifier : Problem(URI("$PROBLEM_URI_PATH/invalid-exclusion-identifier"))

    data object InvalidExclusionType : Problem(URI("$PROBLEM_URI_PATH/invalid-exclusion-type"))

    data object InvalidExclusionIdentifierFormat : Problem(URI("$PROBLEM_URI_PATH/invalid-exclusion-identifier-format"))

    data object InvalidExclusionTypeFormat : Problem(URI("$PROBLEM_URI_PATH/invalid-exclusion-type-format"))

    data object EventAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/event-already-exists"))

    data object EventNotFound : Problem(URI("$PROBLEM_URI_PATH/event-not-found"))

    data object EventNameCannotBeBlank : Problem(URI("$PROBLEM_URI_PATH/event-name-cannot-be-blank"))

    data object LocationAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/location-already-exists"))

    data object LocationNotFound : Problem(URI("$PROBLEM_URI_PATH/location-not-found"))

    data object InvalidLocationName : Problem(URI("$PROBLEM_URI_PATH/invalid-location-name"))

    data object InvalidLocationCoordinates : Problem(URI("$PROBLEM_URI_PATH/invalid-location-coordinates"))

    data object InvalidLocationRadius : Problem(URI("$PROBLEM_URI_PATH/invalid-location-radius"))
}
