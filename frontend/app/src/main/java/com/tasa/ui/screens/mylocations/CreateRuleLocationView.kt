package com.tasa.ui.screens.mylocations

import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.domain.Location
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateRuleLocationView(
    location: Location,
    onCreate: (Location, LocalDateTime, LocalDateTime) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit = {},
) {
    val context = LocalContext.current
    val now = remember { LocalDateTime.now().withSecond(0).withNano(0) }
    var startDateTime by remember { mutableStateOf(now) }
    var endDateTime by remember { mutableStateOf(now.plusHours(1)) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf("start-date") }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    val isValid = startDateTime <= endDateTime

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Nova Regra de Localização",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))

        SectionHeader("Início")
        DateTimePickers(
            date = startDateTime.format(dateFormatter),
            time = startDateTime.format(timeFormatter),
            onDateClick = {
                selectedType = "start-date"
                showDatePicker = true
            },
            onTimeClick = {
                selectedType = "start-time"
                showTimePicker = true
            },
        )

        Spacer(Modifier.height(24.dp))

        SectionHeader("Fim")
        DateTimePickers(
            date = endDateTime.format(dateFormatter),
            time = endDateTime.format(timeFormatter),
            onDateClick = {
                selectedType = "end-date"
                showDatePicker = true
            },
            onTimeClick = {
                selectedType = "end-time"
                showTimePicker = true
            },
        )

        Spacer(Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancelar")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { onCreate(location, startDateTime, endDateTime) },
                modifier = Modifier.weight(1f),
                enabled = isValid,
            ) {
                Text("Criar")
            }
        }
    }

    if (showDatePicker) {
        ShowDatePickerDialog(
            initialDateTime = if (selectedType.contains("start")) startDateTime else endDateTime,
            onDateSelected = { pickedDate ->
                if (selectedType == "start-date") {
                    startDateTime =
                        startDateTime.withYear(pickedDate.year)
                            .withMonth(pickedDate.monthValue)
                            .withDayOfMonth(pickedDate.dayOfMonth)
                    if (startDateTime.isAfter(endDateTime)) endDateTime = startDateTime.plusHours(1)
                } else {
                    endDateTime =
                        endDateTime.withYear(pickedDate.year)
                            .withMonth(pickedDate.monthValue)
                            .withDayOfMonth(pickedDate.dayOfMonth)
                    if (endDateTime.isBefore(startDateTime)) startDateTime = endDateTime.minusHours(1)
                }
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        TimePickerDialogWrapper(
            context = context,
            initialTime = if (selectedType.contains("start")) startDateTime else endDateTime,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { pickedDateTime ->
                if (selectedType == "start-time") {
                    startDateTime =
                        startDateTime.withHour(pickedDateTime.hour)
                            .withMinute(pickedDateTime.minute)
                    if (startDateTime.isAfter(endDateTime)) endDateTime = startDateTime.plusHours(1)
                } else {
                    endDateTime =
                        endDateTime.withHour(pickedDateTime.hour)
                            .withMinute(pickedDateTime.minute)
                    if (endDateTime.isBefore(startDateTime)) startDateTime = endDateTime.minusHours(1)
                }
                showTimePicker = false
            },
        )
    }
}

@Composable
fun TimePickerDialogWrapper(
    context: Context,
    initialTime: LocalDateTime,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalDateTime) -> Unit,
) {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, initialTime.hour)
    calendar.set(Calendar.MINUTE, initialTime.minute)

    DisposableEffect(Unit) {
        val picker =
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val newTime = initialTime.withHour(hourOfDay).withMinute(minute)
                    onTimeSelected(newTime)
                    onDismiss()
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true,
            )

        picker.setOnDismissListener { onDismiss() }
        picker.show()

        onDispose { picker.dismiss() }
    }
}

@Composable
fun ShowDatePickerDialog(
    initialDateTime: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val datePicker =
            android.app.DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selected = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0)
                    if (selected.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                        onDismiss()
                    } else {
                        onDateSelected(selected)
                    }
                },
                initialDateTime.year,
                initialDateTime.monthValue - 1,
                initialDateTime.dayOfMonth,
            )

        // Bloqueia dias anteriores
        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.setOnDismissListener { onDismiss() }
        datePicker.show()

        onDispose { datePicker.dismiss() }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateRuleLocationViewPreview() {
    val loc1 =
        Location(
            id = 1,
            name = "Cinema",
            latitude = 38.736946,
            longitude = -9.142685,
            radius = 50.0,
        )

    CreateRuleLocationView(
        location = loc1,
        onCreate = { _, _, _ -> },
        onCancel = {},
    )
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.secondary,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
    )
}

@Composable
private fun DateTimePickers(
    date: String,
    time: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onDateClick,
        ) {
            Text(date)
        }
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = onTimeClick,
        ) {
            Text(time)
        }
    }
}
