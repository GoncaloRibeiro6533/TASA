package pt.isel.errorHandlers

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import pt.isel.LocationError
import pt.isel.models.Problem
import java.util.Locale

@Component
class LocationErrorHandler(
    private val messageSource: MessageSource,
) {
    private val locale: Locale
        get() = LocaleContextHolder.getLocale()

    fun toResponse(
        error: LocationError,
        input: String = "",
    ): ResponseEntity<Any> {
        val msg = { key: String -> messageSource.getMessage(key, arrayOf(input), locale) }

        return when (error) {
            is LocationError.InvalidLocationName ->
                Problem.InvalidLocationName.response(HttpStatus.BAD_REQUEST, msg("error.location.invalid.name"))

            is LocationError.AlreadyExists ->
                Problem.LocationAlreadyExists.response(HttpStatus.BAD_REQUEST, msg("error.location.already.exists"))

            is LocationError.InvalidLocationCoordinates ->
                Problem.InvalidLocationCoordinates.response(
                    HttpStatus.BAD_REQUEST,
                    msg("error.location.invalid.coordinates"),
                )

            is LocationError.InvalidLocationRadius ->
                Problem.InvalidLocationRadius.response(HttpStatus.BAD_REQUEST, msg("error.location.invalid.radius"))

            is LocationError.LocationNotFound ->
                Problem.LocationNotFound.response(HttpStatus.NOT_FOUND, msg("error.location.not.found"))

            is LocationError.NegativeIdentifier ->
                Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST, msg("error.location.negative.id"))

            is LocationError.NotAllowed ->
                Problem.NotAllowed.response(HttpStatus.FORBIDDEN, msg("error.location.not.allowed"))
            is LocationError.UserNotFound ->
                Problem.UserNotFound.response(HttpStatus.NOT_FOUND, msg("error.location.user.not.found"))
        }
    }
}
