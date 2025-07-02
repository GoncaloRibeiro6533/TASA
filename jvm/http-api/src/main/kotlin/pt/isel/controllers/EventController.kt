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
import pt.isel.EventService
import pt.isel.Failure
import pt.isel.Success
import pt.isel.errorHandlers.EventErrorHandler
import pt.isel.models.event.EventInput

@RestController
@RequestMapping("api/event")
class EventController(
    private val eventService: EventService,
    private val eventErrorHandler: EventErrorHandler,
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
            is Failure ->
                eventErrorHandler.toResponse(
                    eventError = result.value,
                    input = eventIn.title,
                )
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
            is Failure -> eventErrorHandler.toResponse(result.value)
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
            is Failure -> eventErrorHandler.toResponse(result.value)
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
            is Failure -> eventErrorHandler.toResponse(result.value, eventIn.title)
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
            is Failure -> eventErrorHandler.toResponse(result.value, id.toString())
        }
    }
}
