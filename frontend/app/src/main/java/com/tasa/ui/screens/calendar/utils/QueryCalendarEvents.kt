package com.tasa.ui.screens.calendar.utils

import android.content.Context
import android.provider.CalendarContract
import com.tasa.domain.CalendarEvent
import com.tasa.domain.Event
import com.tasa.domain.toLocalDateTime
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.Locale

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
            val language = Locale.getDefault().language.toString()
            val keywords =
                when (language) {
                    "pt" -> listOf("feriado", "comemoração", "data comemorativa", "feriados")
                    "en" -> listOf("holiday", "celebration", "commemorative date", "holidays")
                    "es" -> listOf("feriado", "celebración", "fecha conmemorativa", "días festivos")
                    "fr" -> listOf("jour férié", "célébration", "date commémorative", "jours fériés")
                    "de" -> listOf("feiertag", "feier", "gedenktag", "feiertage")
                    "it" -> listOf("giorno festivo", "celebrazione", "data commemorativa", "festività")
                    "ja" -> listOf("祝日", "記念日", "祝賀", "休日")
                    "zh" -> listOf("节假日", "庆祝活动", "纪念日", "假期")
                    "ru" -> listOf("праздник", "торжество", "памятная дата", "праздники")
                    "ar" -> listOf("عيد", "احتفال", "تاريخ احتفالي", "أعياد")
                    "hi" -> listOf("त्योहार", "उत्सव", "समारोह", "छुट्टियाँ")
                    "ko" -> listOf("공휴일", "기념일", "축하 행사", "휴일")
                    "tr" -> listOf("tatil", "kutlama", "anma günü", "tatiller")
                    "nl" -> listOf("feestdag", "viering", "herdenkingsdatum", "feestdagen")
                    "sv" -> listOf("helgdag", "firande", "minnesdag", "helgdagar")
                    "pl" -> listOf("święto", "uroczystość", "data pamiątkowa", "święta")
                    "da" -> listOf("helligdag", "fejring", "minde dag", "helligdage")
                    "fi" -> listOf("juhlapäivä", "juhla", "muistopäivä", "juhlapäivät")
                    "no" -> listOf("helligdag", "feiring", "minnedag", "helligdager")
                    "cs" -> listOf("svátek", "oslava", "pamětní den", "svátky")
                    "hu" -> listOf("ünnepnap", "ünneplés", "emléknap", "ünnepek")
                    "ro" -> listOf("sărbătoare", "eveniment", "zi comemorativă", "sărbători")
                    "bg" -> listOf("празник", "тържество", "паметна дата", "празници")
                    "uk" -> listOf("свято", "урочистість", "пам'ятна дата", "свята")
                    "el" -> listOf("εορτή", "γιορτή", "επέτειος", "εορτές")
                    else -> listOf("feriado", "comemoração", "data comemorativa", "feriados")
                }
            if (keywords.any { description.contains(it) }) {
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

fun Context.toLocalEvent(
    externalId: Int,
    title: String,
    startTime: LocalDateTime,
    endTime: LocalDateTime,
): Event? {
    val projection =
        arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.CALENDAR_ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.DESCRIPTION,
        )

    val selection =
        """
        ${CalendarContract.Events.DTSTART} = ? AND
        ${CalendarContract.Events.DTEND} = ? AND
        ${CalendarContract.Events.TITLE} = ?
        """.trimIndent()

    val selectionArgs =
        arrayOf(
            startTime.toEpochSecond(ZoneOffset.UTC).toString(),
            endTime.toEpochSecond(ZoneOffset.UTC).toString(),
            title,
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
            val language = Locale.getDefault().displayLanguage
            val keywords =
                when (language) {
                    "pt" -> listOf("feriado", "comemoração", "data comemorativa", "feriados")
                    "en" -> listOf("holiday", "celebration", "commemorative date", "holidays")
                    "es" -> listOf("feriado", "celebración", "fecha conmemorativa", "días festivos")
                    "fr" -> listOf("jour férié", "célébration", "date commémorative", "jours fériés")
                    "de" -> listOf("feiertag", "feier", "gedenktag", "feiertage")
                    "it" -> listOf("giorno festivo", "celebrazione", "data commemorativa", "festività")
                    "ja" -> listOf("祝日", "記念日", "祝賀", "休日")
                    "zh" -> listOf("节假日", "庆祝活动", "纪念日", "假期")
                    "ru" -> listOf("праздник", "торжество", "памятная дата", "праздники")
                    "ar" -> listOf("عيد", "احتفال", "تاريخ احتفالي", "أعياد")
                    "hi" -> listOf("त्योहार", "उत्सव", "समारोह", "छुट्टियाँ")
                    "ko" -> listOf("공휴일", "기념일", "축하 행사", "휴일")
                    "tr" -> listOf("tatil", "kutlama", "anma günü", "tatiller")
                    "nl" -> listOf("feestdag", "viering", "herdenkingsdatum", "feestdagen")
                    "sv" -> listOf("helgdag", "firande", "minnesdag", "helgdagar")
                    "pl" -> listOf("święto", "uroczystość", "data pamiątkowa", "święta")
                    "da" -> listOf("helligdag", "fejring", "minde dag", "helligdage")
                    "fi" -> listOf("juhlapäivä", "juhla", "muistopäivä", "juhlapäivät")
                    "no" -> listOf("helligdag", "feiring", "minnedag", "helligdager")
                    "cs" -> listOf("svátek", "oslava", "pamětní den", "svátky")
                    "hu" -> listOf("ünnepnap", "ünneplés", "emléknap", "ünnepek")
                    "ro" -> listOf("sărbătoare", "eveniment", "zi comemorativă", "sărbători")
                    "bg" -> listOf("празник", "тържество", "паметна дата", "празници")
                    "uk" -> listOf("свято", "урочистість", "пам'ятна дата", "свята")
                    "el" -> listOf("εορτή", "γιορτή", "επέτειος", "εορτές")
                    else -> listOf("feriado", "comemoração", "data comemorativa", "feriados")
                }
            if (keywords.any { description.contains(it) }) {
                continue
            }
            return Event(
                id = externalId,
                eventId = it.getLong(idIndex),
                calendarId = it.getLong(calendarIdIndex),
                title = it.getString(titleIndex) ?: "Sem título",
            )
        }
    }
    return null
}
