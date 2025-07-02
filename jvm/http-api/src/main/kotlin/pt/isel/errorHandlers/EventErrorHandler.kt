package pt.isel.errorHandlers

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import pt.isel.EventError
import pt.isel.models.Problem
import java.util.Locale

@Component
class EventErrorHandler(
    private val messageSource: MessageSource,
) {
    private val locale: Locale
        get() = LocaleContextHolder.getLocale()

    fun toResponse(
        eventError: EventError,
        input: String = "",
    ): ResponseEntity<*> {
        val msg = { key: String -> messageSource.getMessage(key, arrayOf(input), locale) }

        return when (eventError) {
            is EventError.AlreadyExists ->
                Problem.EventAlreadyExists.response(HttpStatus.CONFLICT, msg("error.event.already.exists"))

            is EventError.EventNotFound ->
                Problem.EventNotFound.response(HttpStatus.NOT_FOUND, msg("error.event.not.found"))

            is EventError.EventNameCannotBeBlank ->
                Problem.EventNameCannotBeBlank.response(HttpStatus.BAD_REQUEST, msg("error.event.name.blank"))

            is EventError.NegativeIdentifier ->
                Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST, msg("error.event.id.negative"))

            is EventError.NotAllowed ->
                Problem.NotAllowed.response(HttpStatus.FORBIDDEN, msg("error.event.not.allowed"))

            is EventError.UserNotFound ->
                Problem.UserNotFound.response(HttpStatus.NOT_FOUND, msg("error.event.user.not.found"))
        }
    }
}
