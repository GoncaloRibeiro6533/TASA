package com.tasa.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import com.tasa.domain.CalendarEvent
import com.tasa.domain.Event
import com.tasa.domain.toLocalDateTime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale

interface QueryCalendarService {
    /**
     * Queries the calendar for events.
     *
     * @return A list of calendar events.
     */
    fun calendarEventsFlow(): Flow<List<CalendarEvent>>

    /**
     * Queries the calendar for events by title and time range.
     *
     * @param externalId The external ID of the event.
     * @param title The title of the event.
     * @param startTime The start time of the event.
     * @param endTime The end time of the event.
     * @return A list of calendar events matching the ID.
     */
    fun toLocalEvent(
        externalId: Int,
        title: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Event?

    fun getEvent(
        eventId: Long,
        calendarId: Long,
    ): CalendarEvent?
}

class QueryCalendarServiceImpl(
    private val contentResolver: ContentResolver,
) : QueryCalendarService {
    override fun toLocalEvent(
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
            ${CalendarContract.Events.TITLE} = ?
            """.trimIndent()

        val selectionArgs =
            arrayOf(
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
            val descriptionIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
            val dtStartIndex = it.getColumnIndex(CalendarContract.Events.DTSTART)
            val dtEndIndex = it.getColumnIndex(CalendarContract.Events.DTEND)

            while (it.moveToNext()) {
                val description = it.getString(descriptionIndex)?.lowercase() ?: ""
                val language = Locale.getDefault().language.split('_').first()
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

        return insertEvent(
            title = title,
            description = "",
            startTime = startTime,
            endTime = endTime,
        )?.let { (eventId, calendarId) ->
            Event(
                id = externalId,
                eventId = eventId,
                calendarId = calendarId,
                title = title,
            )
        }
    }

    fun insertEvent(
        title: String,
        description: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
    ): Pair<Long, Long>? {
        val calendarId =
            getPrimaryCalendarId() ?: run {
                return null
            }
        val startMillis = startTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val values =
            ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.EVENT_TIMEZONE, java.util.TimeZone.getDefault().id)
                put(CalendarContract.Events.EVENT_END_TIMEZONE, java.util.TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 0)
                put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
            }

        return try {
            val uri: Uri? = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                eventId to calendarId
            } else {
                null
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun getPrimaryCalendarId(): Long? {
        val projection =
            arrayOf(
                CalendarContract.Calendars._ID,
            )

        val selection = "${CalendarContract.Calendars.IS_PRIMARY} = 1"

        return contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            selection,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getLong(cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID))
            } else {
                null
            }
        }
    }

    override fun calendarEventsFlow(): Flow<List<CalendarEvent>> =
        callbackFlow {
            val contentUri: Uri = CalendarContract.Events.CONTENT_URI
            val observer =
                object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        trySend(contentResolver.queryCalendarEvents())
                    }
                }
            contentResolver.registerContentObserver(
                contentUri,
                true,
                observer,
            )
            trySend(contentResolver.queryCalendarEvents())
            awaitClose {
                contentResolver.unregisterContentObserver(observer)
            }
        }.distinctUntilChanged()

    private fun ContentResolver.queryCalendarEvents(): List<CalendarEvent> {
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
            this.query(
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

    override fun getEvent(
        eventId: Long,
        calendarId: Long,
    ): CalendarEvent? {
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
            ${CalendarContract.Events._ID} = ? AND
            ${CalendarContract.Events.CALENDAR_ID} = ?
            """.trimIndent()

        val selectionArgs =
            arrayOf(
                eventId.toString(),
                calendarId.toString(),
            )

        val cursor =
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null,
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

                return CalendarEvent(
                    eventId = it.getLong(idIndex),
                    calendarId = it.getLong(calendarIdIndex),
                    title = it.getString(titleIndex) ?: "Sem título",
                    startTime = it.getLong(dtStartIndex).toLocalDateTime(),
                    endTime = it.getLong(dtEndIndex).toLocalDateTime(),
                )
            }
        }
        return null
    }
}
