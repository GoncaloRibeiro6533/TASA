package com.tasa.ui.screens.calendar.utils

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import com.tasa.domain.CalendarEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

fun Context.calendarEventsFlow(): Flow<List<CalendarEvent>> =
    callbackFlow {
        val contentUri: Uri = CalendarContract.Events.CONTENT_URI
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    trySend(queryCalendarEvents())
                }
            }
        contentResolver.registerContentObserver(
            contentUri,
            true,
            observer,
        )
        trySend(queryCalendarEvents())
        awaitClose {
            contentResolver.unregisterContentObserver(observer)
        }
    }.distinctUntilChanged()
