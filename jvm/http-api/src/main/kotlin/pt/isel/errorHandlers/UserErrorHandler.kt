package pt.isel.errorHandlers

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import pt.isel.UserError
import pt.isel.models.Problem
import java.util.Locale

@Component
class UserErrorHandler(
    private val messageSource: MessageSource,
) {
    private val locale: Locale
        get() = LocaleContextHolder.getLocale()

    fun toResponse(userError: UserError): ResponseEntity<*> =
        when (userError) {
            is UserError.SessionExpired ->
                Problem.SessionExpired.response(HttpStatus.UNAUTHORIZED, message("error.session.expired", locale))

            is UserError.NegativeIdentifier ->
                Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST, message("error.negative.identifier", locale))

            is UserError.UserNotFound ->
                Problem.UserNotFound.response(HttpStatus.NOT_FOUND, message("error.user.not.found", locale))

            is UserError.UsernameAlreadyExists ->
                Problem.UsernameAlreadyInUse.response(HttpStatus.CONFLICT, message("error.username.already.exists", locale))

            is UserError.UsernameCannotBeBlank ->
                Problem.UsernameCannotBeBlank.response(HttpStatus.BAD_REQUEST, message("error.username.blank", locale))

            is UserError.PasswordCannotBeBlank ->
                Problem.PasswordCannotBeBlank.response(HttpStatus.BAD_REQUEST, message("error.password.blank", locale))

            is UserError.EmailCannotBeBlank ->
                Problem.EmailCannotBeBlank.response(HttpStatus.BAD_REQUEST, message("error.email.blank", locale))

            is UserError.InvalidEmail ->
                Problem.InvalidEmail.response(HttpStatus.BAD_REQUEST, message("error.email.invalid", locale))

            is UserError.EmailAlreadyInUse ->
                Problem.EmailAlreadyInUse.response(HttpStatus.CONFLICT, message("error.email.in.use", locale))

            is UserError.UsernameToLong ->
                Problem.UsernameToLong.response(HttpStatus.BAD_REQUEST, message("error.username.too.long", locale))

            is UserError.NoMatchingUsername ->
                Problem.NoMatchingUsername.response(HttpStatus.NOT_FOUND, message("error.username.no.match", locale))

            is UserError.NoMatchingPassword ->
                Problem.NoMatchingPassword.response(HttpStatus.BAD_REQUEST, message("error.password.no.match", locale))

            is UserError.WeakPassword ->
                Problem.WeakPassword.response(HttpStatus.BAD_REQUEST, message("error.password.weak", locale))

            is UserError.NegativeLimit ->
                Problem.NegativeLimit.response(HttpStatus.BAD_REQUEST, message("error.limit.negative", locale))

            is UserError.NegativeSkip ->
                Problem.NegativeSkip.response(HttpStatus.BAD_REQUEST, message("error.skip.negative", locale))

            is UserError.InvalidTokenFormat ->
                Problem.InvalidTokenFormat.response(HttpStatus.BAD_REQUEST, message("error.token.invalid", locale))

            is UserError.UsernameToShort ->
                Problem.UsernameTooShort.response(HttpStatus.BAD_REQUEST, message("error.username.too.short", locale))
        }

    private fun message(
        code: String,
        locale: Locale,
    ): String = messageSource.getMessage(code, null, locale)
}
