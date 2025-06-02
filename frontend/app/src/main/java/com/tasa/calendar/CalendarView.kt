package com.tasa.calendar

import android.Manifest
import android.content.Context
import android.provider.CalendarContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tasa.calendar.components.EventCard
import com.tasa.domain.Event
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarEventView(
    onAddEvent: (Event) -> Unit
) {
    val context = LocalContext.current
    var events by remember { mutableStateOf(listOf<String>()) }
    var hasPermission by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
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
                showDialog = true
            }) {
                Text("Show Events")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Upcoming Events") },
                    text = {
                        if (events.isEmpty()) {
                            Text("No events loaded.")
                        } else {
                            Column(modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 300.dp)
                                .verticalScroll(rememberScrollState())
                            ) {
                                events.forEach { event ->
                                    EventCard(eventName = event, onAddEvent = { onAddEvent(Event(1, 1, event)) })
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}


fun getCalendarEvents(context: Context): List<String> {
    val events = mutableListOf<String>()
    val projection =
        arrayOf(
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART,
        )

    val selection = "${CalendarContract.Events.DTSTART} >= ?"
    val selectionArgs = arrayOf(System.currentTimeMillis().toString())

    val cursor =
        context.contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Events.DTSTART} ASC",
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
