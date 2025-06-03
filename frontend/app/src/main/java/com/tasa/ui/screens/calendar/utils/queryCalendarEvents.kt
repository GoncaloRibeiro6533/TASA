package com.tasa.ui.screens.calendar.utils

import android.content.Context
import android.provider.CalendarContract
import com.tasa.domain.CalendarEvent
import com.tasa.domain.toLocalDateTime

fun Context.queryCalendarEvents(): List<CalendarEvent> {
    val events = mutableListOf<CalendarEvent>()
    val projection =
        arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DESCRIPTION,
        )

    val startMillis = System.currentTimeMillis()
    val selection =
        """
        ${CalendarContract.Events.DTSTART} >= ? 
        """.trimIndent()

    val selectionArgs =
        arrayOf(
            startMillis.toString(),
        )

    val cursor =
        contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC",
        )

    cursor?.use {
        val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
        val calendarIdIndex = it.getColumnIndex(CalendarContract.Events.CALENDAR_ID)
        val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
        val dtStartIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
        val dtEndIndex = it.getColumnIndex(CalendarContract.Events.DTEND)
        val descriptionIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)

        while (it.moveToNext()) {
            val description = it.getString(descriptionIndex)?.lowercase() ?: ""
            if (description.contains("feriado") || description.contains("comemoração") ||
                description.contains("data comemorativa") || description.contains("feriados")
            ) {
                continue
            }

            val event =
                CalendarEvent(
                    eventId = it.getLong(idIndex),
                    calendarId = it.getLong(calendarIdIndex),
                    title = it.getString(titleIndex) ?: "Sem título",
                    startTime = it.getLong(dtStartIndex).toLocalDateTime(),
                    endTime = it.getLong(dtEndIndex).toLocalDateTime(),
                )
            events.add(event)
        }
    }

    return events
}
