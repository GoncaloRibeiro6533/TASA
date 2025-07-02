package pt.isel.errorHandlers

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import pt.isel.RuleError
import pt.isel.models.Problem
import java.util.Locale

@Component
class RuleErrorHandler(
    private val messageSource: MessageSource,
) {
    private val locale: Locale
        get() = LocaleContextHolder.getLocale()

    fun toResponse(
        ruleError: RuleError,
        input: String = "",
    ): ResponseEntity<*> {
        val msg = { key: String -> messageSource.getMessage(key, arrayOf(input), locale) }

        return when (ruleError) {
            is RuleError.NegativeIdentifier ->
                Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST, msg("error.rule.negative.identifier"))

            is RuleError.InvalidRadius ->
                Problem.InvalidRadius.response(HttpStatus.BAD_REQUEST, msg("error.rule.invalid.radius"))

            is RuleError.RuleNotFound ->
                Problem.RuleNotFound.response(HttpStatus.NOT_FOUND, msg("error.rule.not.found"))

            is RuleError.RuleAlreadyExistsForGivenTime ->
                Problem.RuleAlreadyExistsForGivenTime.response(HttpStatus.CONFLICT, msg("error.rule.already.exists.for.given.time"))

            is RuleError.EndTimeMustBeBeforeEndTime ->
                Problem.EndTimeMustBeBeforeStartTime.response(HttpStatus.BAD_REQUEST, msg("error.rule.end.time.must.be.before.start.time"))

            is RuleError.InvalidCoordinate ->
                Problem.InvalidCoordinate.response(HttpStatus.BAD_REQUEST, msg("error.rule.invalid.coordinate"))

            is RuleError.InvalidLatitude ->
                Problem.InvalidLatitude.response(HttpStatus.BAD_REQUEST, msg("error.rule.invalid.latitude"))

            is RuleError.InvalidLongitude ->
                Problem.InvalidLongitude.response(HttpStatus.BAD_REQUEST, msg("error.rule.invalid.longitude"))

            is RuleError.NotAllowed ->
                Problem.NotAllowed.response(HttpStatus.FORBIDDEN, msg("error.rule.not.allowed"))

            is RuleError.StartTimeMustBeBeforeEndTime ->
                Problem.StartTimeMustBeBeforeEndTime.response(HttpStatus.BAD_REQUEST, msg("error.rule.start.time.must.be.before.end.time"))

            is RuleError.TitleCannotBeBlank ->
                Problem.TitleCannotBeBlank.response(HttpStatus.BAD_REQUEST, msg("error.rule.title.cannot.be.blank"))

            is RuleError.UserNotFound ->
                Problem.UserNotFound.response(HttpStatus.NOT_FOUND, msg("error.rule.user.not.found"))

            is RuleError.EventNotFound ->
                Problem.EventNotFound.response(HttpStatus.NOT_FOUND, msg("error.rule.event.not.found"))

            is RuleError.LocationNotFound ->
                Problem.LocationNotFound.response(HttpStatus.NOT_FOUND, msg("error.rule.location.not.found"))
        }
    }
}
