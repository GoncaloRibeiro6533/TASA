package com.tasa.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.domain.CalendarEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRulEventView(
    event: CalendarEvent,
    onCreate: (CalendarEvent, LocalDateTime, LocalDateTime) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit = {},
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val fullFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    // Gera intervalos de 30 minutos entre o start e o end
    val timeSlots =
        remember(event) {
            val slots = mutableListOf<LocalDateTime>()
            var time = event.startTime
            while (time <= event.endTime) {
                slots.add(time)
                time = time.plusMinutes(30)
            }
            slots
        }

    var startTime by remember { mutableStateOf(event.startTime) }
    var endTime by remember { mutableStateOf(event.endTime) }

    var expandedStart by remember { mutableStateOf(false) }
    var expandedEnd by remember { mutableStateOf(false) }

    val valid = !startTime.isAfter(endTime)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Criar Regra para Evento", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text(event.title, style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Início do evento: ${event.startTime.format(fullFormatter)}")
        Text("Fim do evento: ${event.endTime.format(fullFormatter)}")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Hora de início da regra", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedStart,
            onExpandedChange = { expandedStart = !expandedStart },
        ) {
            OutlinedTextField(
                value = startTime.format(timeFormatter),
                onValueChange = {},
                readOnly = true,
                label = { Text("Início") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStart)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expandedStart,
                onDismissRequest = { expandedStart = false },
            ) {
                timeSlots.forEach { time ->
                    DropdownMenuItem(
                        text = { Text(time.format(timeFormatter)) },
                        onClick = {
                            startTime = time
                            if (endTime.isBefore(time)) {
                                endTime = time
                            }
                            expandedStart = false
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Hora de fim da regra", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = expandedEnd,
            onExpandedChange = { expandedEnd = !expandedEnd },
        ) {
            OutlinedTextField(
                value = endTime.format(timeFormatter),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fim") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEnd)
                },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
            )

            ExposedDropdownMenu(
                expanded = expandedEnd,
                onDismissRequest = { expandedEnd = false },
            ) {
                timeSlots
                    .filter { !it.isBefore(startTime) }
                    .forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time.format(timeFormatter)) },
                            onClick = {
                                endTime = time
                                expandedEnd = false
                            },
                        )
                    }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    onCreate(event, startTime, endTime)
                },
                enabled = valid,
            ) {
                Text("Criar")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewCreateRulEventView() {
    val now = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
    val event =
        CalendarEvent(
            eventId = 42,
            calendarId = 10,
            title = "Workshop Android Jetpack",
            startTime = now.plusMinutes(30),
            endTime = now.plusHours(2),
        )
    CreateRulEventView(event = event)
}
