package pt.isel

import org.springframework.context.support.StaticMessageSource
import java.util.Locale

fun createTestMessageSource(): StaticMessageSource {
    return StaticMessageSource().apply {
        // User-related errors
        addMessage("error.session.expired", Locale.ENGLISH, "Session expired. Please log in again.")
        addMessage("error.negative.identifier", Locale.ENGLISH, "Identifier cannot be negative.")
        addMessage("error.user.not.found", Locale.ENGLISH, "User with given identifier not found.")
        addMessage("error.username.already.exists", Locale.ENGLISH, "Username already exists.")
        addMessage("error.username.blank", Locale.ENGLISH, "Username cannot be blank.")
        addMessage("error.password.blank", Locale.ENGLISH, "Password cannot be blank.")
        addMessage("error.email.blank", Locale.ENGLISH, "Email cannot be blank.")
        addMessage("error.email.invalid", Locale.ENGLISH, "Invalid email format.")
        addMessage("error.email.in.use", Locale.ENGLISH, "Email is already in use.")
        addMessage("error.username.too.long", Locale.ENGLISH, "Username is too long.")
        addMessage("error.username.no.match", Locale.ENGLISH, "No user found with the given username.")
        addMessage("error.password.no.match", Locale.ENGLISH, "No matching password for the given username.")
        addMessage(
            "error.password.weak",
            Locale.ENGLISH,
            "Password is too weak. It must contain at least 8 characters, including uppercase, lowercase, numbers, and special characters.",
        )
        addMessage("error.username.too.short", Locale.ENGLISH, "Username is too short. It must be at least 3 characters long.")

        // General validation errors
        addMessage("error.limit.negative", Locale.ENGLISH, "Limit cannot be negative.")
        addMessage("error.skip.negative", Locale.ENGLISH, "Skip cannot be negative.")
        addMessage("error.token.invalid", Locale.ENGLISH, "Invalid token format.")

        // Event-related errors
        addMessage("error.event.already.exists", Locale.ENGLISH, "Event with the given title already exists.")
        addMessage("error.event.not.found", Locale.ENGLISH, "Event with given ID not found.")
        addMessage("error.event.name.blank", Locale.ENGLISH, "Event title cannot be blank.")
        addMessage("error.event.id.negative", Locale.ENGLISH, "Identifier cannot be negative.")
        addMessage("error.event.not.allowed", Locale.ENGLISH, "Not allowed to perform this action.")
        addMessage("error.event.user.not.found", Locale.ENGLISH, "User not found.")

        // Location-related errors
        addMessage("error.location.invalid.name", Locale.ENGLISH, "Invalid location name.")
        addMessage("error.location.already.exists", Locale.ENGLISH, "Location with given name already exists for user.")
        addMessage(
            "error.location.invalid.coordinates",
            Locale.ENGLISH,
            "Invalid location coordinates: latitude must be between -90 and 90, longitude must be between -180 and 180.",
        )
        addMessage("error.location.invalid.radius", Locale.ENGLISH, "Invalid location radius: must be a positive number.")
        addMessage("error.location.not.found", Locale.ENGLISH, "Location with given identifier not found.")
        addMessage("error.location.negative.id", Locale.ENGLISH, "Identifier cannot be negative.")
        addMessage("error.location.not.allowed", Locale.ENGLISH, "User is not allowed to perform this action.")
        addMessage("error.location.user.not.found", Locale.ENGLISH, "User with given identifier not found.")

        // Rule-related errors
        addMessage("error.rule.negative.identifier", Locale.ENGLISH, "Negative identifier is not allowed.")
        addMessage("error.rule.invalid.radius", Locale.ENGLISH, "Invalid radius value. It must be a positive number.")
        addMessage("error.rule.not.found", Locale.ENGLISH, "Rule with given identifier not found.")
        addMessage("error.rule.already.exists.for.given.time", Locale.ENGLISH, "Rule already exists for the given time period.")
        addMessage("error.rule.end.time.must.be.before.start.time", Locale.ENGLISH, "End time must be before start time.")
        addMessage(
            "error.rule.invalid.coordinate",
            Locale.ENGLISH,
            "Invalid coordinate value. Latitude must be between -90 and 90, longitude must be between -180 and 180.",
        )
        addMessage("error.rule.invalid.latitude", Locale.ENGLISH, "Invalid latitude value. It must be between -90 and 90.")
        addMessage("error.rule.invalid.longitude", Locale.ENGLISH, "Invalid longitude value. It must be between -180 and 180.")
        addMessage("error.rule.not.allowed", Locale.ENGLISH, "User is not allowed to perform this action.")
        addMessage("error.rule.start.time.must.be.before.end.time", Locale.ENGLISH, "Start time must be before end time.")
        addMessage("error.rule.title.cannot.be.blank", Locale.ENGLISH, "Title cannot be blank.")
        addMessage("error.rule.user.not.found", Locale.ENGLISH, "User with given identifier not found.")
        addMessage("error.rule.event.not.found", Locale.ENGLISH, "Event with given identifier not found.")
        addMessage("error.rule.location.not.found", Locale.ENGLISH, "Location with given identifier not found.")
    }
}
