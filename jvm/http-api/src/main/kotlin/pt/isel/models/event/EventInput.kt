package pt.isel.models.event

data class EventInput(
    val eventId: Long,
    val calendarId: Long,
    val title: String,
)
