package com.tasa.ui.screens.rule

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.tasa.calendar.toFormattedDate
import com.tasa.domain.Event
import com.tasa.domain.Rule
import com.tasa.domain.RuleEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRuleEventView(
    rule: Rule,
    onUpdate: (LocalDateTime, LocalDateTime) -> Unit = { _, _ -> },
    onCancel: () -> Unit = {},
) {
    val context = LocalContext.current
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    var startTime by remember { mutableStateOf(rule.startTime) }
    var endTime by remember { mutableStateOf(rule.endTime) }

    // 👇 controla se picker aparece
    var showTimePicker by remember { mutableStateOf(false) }

    // 👇 "start" ou "end"
    var selectedTimeType by remember { mutableStateOf("start") }

    val valid = !startTime.isAfter(endTime)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Editar Regra", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Início atual: ${rule.startTime.toFormattedDate()}")
        Text("Fim atual: ${rule.endTime.toFormattedDate()}")

        Spacer(modifier = Modifier.height(24.dp))

        Text("Hora de Início", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedTimeType = "start"
                showTimePicker = true
            },
        ) {
            Text(startTime.format(timeFormatter))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Hora de Fim", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                selectedTimeType = "end"
                showTimePicker = true
            },
        ) {
            Text(endTime.format(timeFormatter))
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
                    onUpdate(startTime, endTime)
                },
                enabled = valid,
            ) {
                Text("Atualizar")
            }
        }
    }
    if (showTimePicker) {
        TimePickerDialogWrapper(
            context = context,
            initialTime = if (selectedTimeType == "start") startTime else endTime,
            onDismiss = { showTimePicker = false },
            onTimeSelected = {
                if (selectedTimeType == "start") {
                    startTime = it
                    if (it.isAfter(endTime)) endTime = it
                } else {
                    endTime = it
                    if (it.isBefore(startTime)) startTime = it
                }
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

@Preview(showBackground = true)
@Composable
fun PreviewEditRuleEventView() {
    val now = LocalDateTime.now().withSecond(0).withNano(0)
    val rule =
        RuleEvent(
            id = 1,
            startTime = now.plusHours(1),
            endTime = now.plusHours(2),
            event =
                Event(
                    id = 42,
                    calendarId = 10,
                    title = "Workshop Android Jetpack",
                ),
        )
    EditRuleEventView(rule = rule)
}
