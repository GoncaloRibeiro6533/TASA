package com.tasa.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.text.SimpleDateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun CalendarEventScreen() {
    val context = LocalContext.current
    var events by remember { mutableStateOf(listOf<String>()) }
    var hasPermission by remember { mutableStateOf(false) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasPermission = isGranted
    }

    LaunchedEffect(Unit) {
        requestPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Calendar Events", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (!hasPermission) {
            Text("Permission not granted.")
        } else {
            Button(onClick = {
                events = getCalendarEvents(context)
            }) {
                Text("Fetch Events")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (events.isEmpty()) {
                Text("No events loaded yet.")
            } else {
                events.forEach { event ->
                    Text("• $event", modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}


fun getCalendarEvents(context: Context): List<String> {
    val events = mutableListOf<String>()
    val projection = arrayOf(
        CalendarContract.Events.TITLE,
        CalendarContract.Events.DTSTART
    )

    val selection = "${CalendarContract.Events.DTSTART} >= ?"
    val selectionArgs = arrayOf(System.currentTimeMillis().toString())

    val cursor = context.contentResolver.query(
        CalendarContract.Events.CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        "${CalendarContract.Events.DTSTART} ASC"
    )

    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    cursor?.use {
        while (it.moveToNext()) {
            val title = it.getString(0) ?: "No Title"
            val startTimeMillis = it.getLong(1)
            val date = dateFormatter.format(Date(startTimeMillis))
            events.add("$title - $date")
        }
    }

    return events
}
