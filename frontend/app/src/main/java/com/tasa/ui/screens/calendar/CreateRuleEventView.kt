package com.tasa.ui.screens.calendar

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.domain.CalendarEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun CreateRuleEventView(
    event: CalendarEvent,
    onCreate: (CalendarEvent, LocalDateTime, LocalDateTime) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit = {},
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        CreateRuleEventHorizontalView(event, onCreate, onCancel)
    } else {
        CreateRuleEventVerticalView(event, onCreate, onCancel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleEventVerticalView(
    event: CalendarEvent,
    onCreate: (CalendarEvent, LocalDateTime, LocalDateTime) -> Unit,
    onCancel: () -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val fullFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }

    val timeSlots = remember(event) {
        generateSequence(event.startTime) { it.plusMinutes(30) }
            .takeWhile { it <= event.endTime }
            .toList()
    }

    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }
    var expandedStart by remember { mutableStateOf(false) }
    var expandedEnd by remember { mutableStateOf(false) }

    val valid = !startTime.isAfter(endTime)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.create_rule_for_event), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text(event.title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("${stringResource(R.string.current_start_time)}: ${event.startTime.format(fullFormatter)}")
        Text("${stringResource(R.string.current_end_time)}: ${event.endTime.format(fullFormatter)}")
        Spacer(modifier = Modifier.height(24.dp))

        DropdownField(
            label = stringResource(R.string.rule_start_time),
            value = startTime,
            onValueSelected = {
                startTime = it
                if (endTime.isBefore(it)) endTime = it
            },
            timeFormatter = timeFormatter,
            timeSlots = timeSlots,
            expanded = expandedStart,
            onExpandedChange = { expandedStart = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        DropdownField(
            label = stringResource(R.string.rule_end_time),
            value = endTime,
            onValueSelected = { endTime = it },
            timeFormatter = timeFormatter,
            timeSlots = timeSlots.filter { !it.isBefore(startTime) },
            expanded = expandedEnd,
            onExpandedChange = { expandedEnd = it }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
            Button(
                onClick = { onCreate(event, startTime, endTime) },
                enabled = valid,
            ) {
                Text(stringResource(R.string.create))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleEventHorizontalView(
    event: CalendarEvent,
    onCreate: (CalendarEvent, LocalDateTime, LocalDateTime) -> Unit,
    onCancel: () -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val fullFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }

    val timeSlots = remember(event) {
        generateSequence(event.startTime) { it.plusMinutes(30) }
            .takeWhile { it <= event.endTime }
            .toList()
    }

    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }
    var expandedStart by remember { mutableStateOf(false) }
    var expandedEnd by remember { mutableStateOf(false) }

    val valid = !startTime.isAfter(endTime)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            Text(stringResource(R.string.create_rule_for_event), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(event.title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("${stringResource(R.string.current_start_time)}: ${event.startTime.format(fullFormatter)}")
            Text("${stringResource(R.string.current_end_time)}: ${event.endTime.format(fullFormatter)}")
        }

        Column(modifier = Modifier.weight(1f)) {
            DropdownField(
                label = stringResource(R.string.rule_start_time),
                value = startTime,
                onValueSelected = {
                    startTime = it
                    if (endTime.isBefore(it)) endTime = it
                },
                timeFormatter = timeFormatter,
                timeSlots = timeSlots,
                expanded = expandedStart,
                onExpandedChange = { expandedStart = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            DropdownField(
                label = stringResource(R.string.rule_end_time),
                value = endTime,
                onValueSelected = { endTime = it },
                timeFormatter = timeFormatter,
                timeSlots = timeSlots.filter { !it.isBefore(startTime) },
                expanded = expandedEnd,
                onExpandedChange = { expandedEnd = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text(stringResource(R.string.cancel))
                }
                Button(
                    onClick = { onCreate(event, startTime, endTime) },
                    enabled = valid
                ) {
                    Text(stringResource(R.string.create))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    value: LocalDateTime,
    onValueSelected: (LocalDateTime) -> Unit,
    timeFormatter: DateTimeFormatter,
    timeSlots: List<LocalDateTime>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onExpandedChange(!expanded) },
    ) {
        OutlinedTextField(
            value = value.format(timeFormatter),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            timeSlots.forEach { time ->
                DropdownMenuItem(
                    text = { Text(time.format(timeFormatter)) },
                    onClick = {
                        onValueSelected(time)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateRuleEvent_Portrait() {
    val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
    val event = CalendarEvent(1, 2, "Preview Portrait", now.plusHours(1), now.plusHours(3))
    CreateRuleEventView(event)
}

@Preview(showBackground = true, widthDp = 720, heightDp = 400)
@Composable
fun PreviewCreateRuleEvent_Landscape() {
    val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
    val event = CalendarEvent(1, 2, "Preview Landscape", now.plusHours(1), now.plusHours(3))
    CreateRuleEventView(event)
}
