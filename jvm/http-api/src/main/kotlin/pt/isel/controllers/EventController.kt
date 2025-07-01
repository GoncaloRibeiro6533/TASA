package pt.isel.controllers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import pt.isel.AuthenticatedUser
import pt.isel.EventError
import pt.isel.EventService
import pt.isel.Failure
import pt.isel.Success
import pt.isel.models.Problem
import pt.isel.models.event.EventInput

@RestController
@RequestMapping("api/event")
class EventController(
    private val eventService: EventService,
) {
    @PostMapping("/create")
    fun createEvent(
        authUser: AuthenticatedUser,
        @RequestBody eventIn: EventInput,
    ): ResponseEntity<*> {
        val result =
            eventService.createEvent(
                title = eventIn.title,
                userId = authUser.user.id,
                startTime = eventIn.startTime,
                endTime = eventIn.endTime,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @GetMapping("/{id}")
    fun getEvent(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result =
            eventService.getEvent(
                eventId = id,
                userId = authUser.user.id,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @GetMapping("/all")
    fun getAllEvents(authUser: AuthenticatedUser): ResponseEntity<*> {
        val result =
            eventService.getEventsOfUser(
                userId = authUser.user.id,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @PutMapping("{id}/update/title")
    fun updateEvent(
        authUser: AuthenticatedUser,
        @RequestBody eventIn: EventInput,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result =
            eventService.updateEvent(
                eventId = id,
                newTitle = eventIn.title,
                userId = authUser.user.id,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @DeleteMapping("/remove/{id}")
    fun deleteEvent(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result =
            eventService.deleteEvent(
                eventId = id,
                userId = authUser.user.id,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure -> result.value.toResponse()
        }
    }

    private fun EventError.toResponse() =
        when (this) {
            is EventError.AlreadyExists -> Problem.EventAlreadyExists.response(HttpStatus.CONFLICT)
            is EventError.EventNotFound -> Problem.EventNotFound.response(HttpStatus.NOT_FOUND)
            is EventError.EventNameCannotBeBlank -> Problem.EventNameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is EventError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
            is EventError.NotAllowed -> Problem.NotAllowed.response(HttpStatus.FORBIDDEN)
            is EventError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
        }
}
