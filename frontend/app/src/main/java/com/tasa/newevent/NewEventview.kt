package com.tasa.newevent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.domain.Event
import com.tasa.newevent.components.AddEventButton
import com.tasa.newevent.components.EventTextFields
import java.util.Calendar

@Composable
fun NewEventView(onAddEvent: (Event) -> Unit) {
    var eventName by rememberSaveable { mutableStateOf("") }
    var startDateTime by rememberSaveable { mutableStateOf("") }
    var endDateTime by rememberSaveable { mutableStateOf("") }
    var isStartDatePickerOpen by remember { mutableStateOf(false) }
    var isEndDatePickerOpen by remember { mutableStateOf(false) }
    var isStartTimePickerOpen by remember { mutableStateOf(false) }
    var isEndTimePickerOpen by remember { mutableStateOf(false) }

    val invalidFields = eventName.isEmpty() || startDateTime.isEmpty() || endDateTime.isEmpty()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
    ) {
        Text(
            text = "New Event",
            modifier = Modifier.padding(bottom = 16.dp),
        )

        EventTextFields(
            eventName = eventName,
            onEventNameChangeCallback = { eventName = it.trim() },
            modifier = Modifier,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Start Date and Time Picker
        Text(text = "Start: $startDateTime", modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = { isStartDatePickerOpen = true }) {
            Text(text = "Pick Start Date")
        }
        Button(onClick = { isStartTimePickerOpen = true }) {
            Text(text = "Pick Start Time")
        }

        // End Date and Time Picker
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "End: $endDateTime", modifier = Modifier.padding(bottom = 8.dp))
        Button(onClick = { isEndDatePickerOpen = true }) {
            Text(text = "Pick End Date")
        }
        Button(onClick = { isEndTimePickerOpen = true }) {
            Text(text = "Pick End Time")
        }

        // DatePickerDialog for Start Date
        if (isStartDatePickerOpen) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                LocalContext.current,
                { _, selectedYear, selectedMonth, selectedDay ->
                    startDateTime = "$selectedDay/${selectedMonth + 1}/$selectedYear $startDateTime"
                    isStartDatePickerOpen = false
                },
                year,
                month,
                day,
            ).show()
        }

        // TimePickerDialog for Start Time
        if (isStartTimePickerOpen) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                LocalContext.current,
                { _, selectedHour, selectedMinute ->
                    startDateTime = "$startDateTime ${String.format("%02d:%02d", selectedHour, selectedMinute)}"
                    isStartTimePickerOpen = false
                },
                hour,
                minute,
                true,
            ).show()
        }

        // DatePickerDialog for End Date
        if (isEndDatePickerOpen) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                LocalContext.current,
                { _, selectedYear, selectedMonth, selectedDay ->
                    endDateTime = "$selectedDay/${selectedMonth + 1}/$selectedYear $endDateTime"
                    isEndDatePickerOpen = false
                },
                year,
                month,
                day,
            ).show()
        }

        // TimePickerDialog for End Time
        if (isEndTimePickerOpen) {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                LocalContext.current,
                { _, selectedHour, selectedMinute ->
                    endDateTime = "$endDateTime ${String.format("%02d:%02d", selectedHour, selectedMinute)}"
                    isEndTimePickerOpen = false
                },
                hour,
                minute,
                true,
            ).show()
        }

        AddEventButton(
            enabled = !invalidFields,
            modifier = Modifier,
        ) {
            val event = Event(1, 1, eventName)
            onAddEvent(event)
        }
    }
}

@Composable
fun EventTextFields(
    eventName: String,
    onEventNameChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = eventName,
        onValueChange = onEventNameChangeCallback,
        label = { Text("Event Name") },
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun AddEventButton(
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(text = "Add Event")
    }
}

@Preview(showBackground = true)
@Composable
fun NewEventViewPreview() {
    NewEventView {}
}
