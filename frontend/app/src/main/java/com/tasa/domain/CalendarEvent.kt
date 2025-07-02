package com.tasa.domain

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CalendarEvent(
    val eventId: Long,
    val calendarId: Long,
    val title: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    val event: Event
        get() {
            return Event(
                eventId = eventId,
                calendarId = calendarId,
                title = title,
            )
        }

    fun getFormattedStartTime(): String = startTime.toFormattedDate()

    fun getFormattedStartTime(pattern: String): String = startTime.toFormattedDate(pattern)

    fun getFormattedEndTime(pattern: String): String = endTime.toFormattedDate(pattern)

    fun getFormattedEndTime(): String = endTime.toFormattedDate()
}

fun LocalDateTime.toFormattedDate(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
    return this.format(formatter)
}

fun Long.toFormattedDate(pattern: String = "dd/MM/yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toLocalDateTime(): LocalDateTime {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return LocalDateTime.of(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND),
    )
}
