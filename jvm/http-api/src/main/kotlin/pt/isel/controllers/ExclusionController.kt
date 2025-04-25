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
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import pt.isel.AppExclusion
import pt.isel.AuthenticatedUser
import pt.isel.ContactExclusion
import pt.isel.ExclusionError
import pt.isel.ExclusionService
import pt.isel.Failure
import pt.isel.Success
import pt.isel.models.Problem
import pt.isel.models.exclusion.AppExclusionInput
import pt.isel.models.exclusion.ContactExclusionInput
import pt.isel.models.exclusion.ExclusionsOutput

@RestController
@RequestMapping("api/exclusion")
class ExclusionController(
    private val exclusionService: ExclusionService,
) {
    @PostMapping("/contact")
    fun createContactExclusion(
        authUser: AuthenticatedUser,
        @RequestBody contactExclusionInput: ContactExclusionInput,
    ): ResponseEntity<*> {
        val result =
            exclusionService.createContactExclusion(
                authUser.user.id,
                contactExclusionInput.contactName,
                contactExclusionInput.contactPhoneNumber,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @PostMapping("/app")
    fun createAppExclusion(
        authUser: AuthenticatedUser,
        @RequestBody appExclusionInput: AppExclusionInput,
    ): ResponseEntity<*> {
        val result =
            exclusionService.createAppExclusion(
                authUser.user.id,
                appExclusionInput.appName,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.CREATED).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @GetMapping("/contact")
    fun getExclusions(authUser: AuthenticatedUser): ResponseEntity<*> {
        val result = exclusionService.getUserExclusions(authUser.user.id)
        return when (result) {
            is Success -> {
                val appExclusions = result.value.filterIsInstance<AppExclusion>()
                val contactExclusions = result.value.filterIsInstance<ContactExclusion>()
                ResponseEntity.status(HttpStatus.OK).body(
                    ExclusionsOutput(
                        nContactExclusions = contactExclusions.size,
                        contactExclusions = contactExclusions,
                        nAppExclusions = appExclusions.size,
                        appExclusions = appExclusions,
                    ),
                )
            }
            is Failure -> result.value.toResponse()
        }
    }

    @PutMapping("/update/contact/{id}")
    fun updateContactExclusion(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
        @RequestParam(name = "contactName", required = false) contactName: String?,
        @RequestParam(name = "contactPhoneNumber", required = false) contactPhoneNumber: String?,
    ): ResponseEntity<*> {
        val result =
            exclusionService.updateContactExclusion(
                authUser.user.id,
                id,
                contactName,
                contactPhoneNumber,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @PutMapping("/update/app/{id}/{name}")
    fun updateAppExclusion(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
        @PathVariable name: String,
    ): ResponseEntity<*> {
        val result =
            exclusionService.updateAppExclusion(
                authUser.user.id,
                id,
                name,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @DeleteMapping("/contact/{id}")
    fun deleteContactExclusion(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result =
            exclusionService.deleteContactExclusion(
                authUser.user.id,
                id,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(null)
            is Failure -> result.value.toResponse()
        }
    }

    @DeleteMapping("/app/{id}")
    fun deleteAppExclusion(
        authUser: AuthenticatedUser,
        @PathVariable id: Int,
    ): ResponseEntity<*> {
        val result =
            exclusionService.deleteAppExclusion(
                authUser.user.id,
                id,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.NO_CONTENT).body(null)
            is Failure -> result.value.toResponse()
        }
    }

    @PutMapping("/contact/event/{exclusionId}/{ruleId}")
    fun addContactExclusionToRuleEvent(
        authUser: AuthenticatedUser,
        @PathVariable exclusionId: Int,
        @PathVariable ruleId: Int,
    ): ResponseEntity<*> {
        val result =
            exclusionService.addContactExclusionToRuleEvent(
                authUser.user,
                exclusionId,
                ruleId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @PutMapping("/app/event/{exclusionId}/{ruleId}")
    fun addAppExclusionToRuleEvent(
        authUser: AuthenticatedUser,
        @PathVariable exclusionId: Int,
        @PathVariable ruleId: Int,
    ): ResponseEntity<*> {
        val result =
            exclusionService.addAppExclusionToRuleEvent(
                authUser.user,
                exclusionId,
                ruleId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @PutMapping("/contact/location/{exclusionId}/{ruleId}")
    fun addContactExclusionToRuleLocation(
        authUser: AuthenticatedUser,
        @PathVariable exclusionId: Int,
        @PathVariable ruleId: Int,
    ): ResponseEntity<*> {
        val result =
            exclusionService.addContactExclusionToRuleLocation(
                authUser.user,
                exclusionId,
                ruleId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }

    @PutMapping("/app/location/{exclusionId}/{ruleId}")
    fun addAppExclusionToRuleLocation(
        authUser: AuthenticatedUser,
        @PathVariable exclusionId: Int,
        @PathVariable ruleId: Int,
    ): ResponseEntity<*> {
        val result =
            exclusionService.addAppExclusionToRuleLocation(
                authUser.user,
                exclusionId,
                ruleId,
            )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure -> result.value.toResponse()
        }
    }
    /*

    @PutMapping("/add/{exclusionId}/rule/{ruleId}")
    fun addAppExclusionToRuleLocation(
        authUser: AuthenticatedUser,
        @PathVariable exclusionId: Int,
        @PathVariable ruleId: Int,
    ): ResponseEntity<*> {
        val result = exclusionService.addExclusionToRule(
            authUser.user.id,
            exclusionId,
            ruleId
        )
        return when (result) {
            is Success -> ResponseEntity.status(HttpStatus.OK).body(result.value)
            is Failure -> result.value.toResponse()
        }
     */

    private fun ExclusionError.toResponse() =
        when (this) {
            is ExclusionError.UserNotFound -> Problem.UserNotFound.response(HttpStatus.NOT_FOUND)
            is ExclusionError.AppNameBlank -> Problem.AppNameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.AppNameTooLong -> Problem.AppNameTooLong.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.BlankContactName -> Problem.ContactNameCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.BlankPhoneNumber -> Problem.ContactPhoneNumberCannotBeBlank.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.ContactNameTooLong -> Problem.ContactNameTooLong.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.ExclusionAlreadyExists -> Problem.ExclusionAlreadyExists.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.ExclusionNotFound -> Problem.ExclusionNotFound.response(HttpStatus.NOT_FOUND)
            is ExclusionError.NegativeIdentifier -> Problem.NegativeIdentifier.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.PhoneNumberTooLong -> Problem.PhoneNumberTooLong.response(HttpStatus.BAD_REQUEST)
            is ExclusionError.NotAllowed -> Problem.NotAllowed.response(HttpStatus.FORBIDDEN)
            is ExclusionError.RuleNotFound -> Problem.RuleNotFound.response(HttpStatus.NOT_FOUND)
        }
}
