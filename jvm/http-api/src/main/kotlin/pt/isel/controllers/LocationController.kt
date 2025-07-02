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
import pt.isel.Either
import pt.isel.Failure
import pt.isel.Location
import pt.isel.LocationError
import pt.isel.LocationService
import pt.isel.Success
import pt.isel.errorHandlers.LocationErrorHandler
import pt.isel.models.location.LocationInput
import pt.isel.models.location.LocationList

@RestController
@RequestMapping("api/location")
class LocationController(
    private val locationService: LocationService,
    private val locationErrorHandler: LocationErrorHandler,
) {
    @PostMapping("/create")
    fun createLocation(
        authUser: AuthenticatedUser,
        @RequestBody locationInput: LocationInput,
    ): ResponseEntity<*> {
        val result: Either<LocationError, Location> =
            locationService.createLocation(
                userId = authUser.user.id,
                name = locationInput.name,
                latitude = locationInput.latitude,
                longitude = locationInput.longitude,
                radius = locationInput.radius,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure -> locationErrorHandler.toResponse(result.value, locationInput.name)
        }
    }

    @GetMapping("/{id}")
    fun getLocation(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result: Either<LocationError, Location> =
            locationService.getLocationById(
                userId = authUser.user.id,
                id = id,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> locationErrorHandler.toResponse(result.value, id.toString())
        }
    }

    @GetMapping("/all")
    fun getUserLocations(authUser: AuthenticatedUser): ResponseEntity<*> {
        val result: Either<LocationError, List<Location>> =
            locationService.getAllLocations(
                userId = authUser.user.id,
            )
        return when (result) {
            is Success ->
                ResponseEntity.ok(
                    LocationList(
                        nLocations = result.value.size,
                        locations = result.value,
                    ),
                )
            is Failure -> {
                locationErrorHandler.toResponse(result.value)
            }
        }
    }

    @PutMapping("/{id}/update/name/{name}")
    fun updateLocationName(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
        @PathVariable name: String,
    ): ResponseEntity<*> {
        val result: Either<LocationError, Location> =
            locationService.updateLocationName(
                userId = authUser.user.id,
                locationId = id,
                name = name,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> {
                locationErrorHandler.toResponse(result.value, name)
            }
        }
    }

    @PutMapping("/{id}/update/radius/{radius}")
    fun updateLocationRadius(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
        @PathVariable radius: Double,
    ): ResponseEntity<*> {
        val result: Either<LocationError, Location> =
            locationService.updateLocationRadius(
                userId = authUser.user.id,
                locationId = id,
                radius = radius,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(result.value)
            is Failure -> {
                locationErrorHandler.toResponse(result.value, radius.toString())
            }
        }
    }

    @DeleteMapping("remove/{id}")
    fun deleteLocation(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result: Either<LocationError, Boolean> =
            locationService.deleteLocation(
                userId = authUser.user.id,
                locationId = id,
            )
        return when (result) {
            is Success -> ResponseEntity.ok(null)
            is Failure -> locationErrorHandler.toResponse(result.value, id.toString())
        }
    }
}
