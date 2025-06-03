package com.tasa.ui.screens.calendar.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectorBar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    val openDatePicker = remember { mutableStateOf(false) }
    val todayInMillis =
        LocalDate.now()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()

    val datePickerState =
        rememberDatePickerState(
            initialSelectedDateMillis = todayInMillis,
            selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                        return utcTimeMillis >= todayInMillis
                    }
                },
        )
    if (openDatePicker.value) {
        DatePickerDialog(
            onDismissRequest = { openDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val instant = Instant.ofEpochMilli(millis)
                        val zone = ZoneId.systemDefault()
                        val offset = zone.rules.getOffset(instant)
                        val adjustedInstant = instant.plusSeconds(offset.totalSeconds.toLong())
                        val pickedDate = adjustedInstant.atZone(zone).toLocalDate()
                        onDateSelected(pickedDate)
                    }

                    openDatePicker.value = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { openDatePicker.value = false }) {
                    Text("Cancelar")
                }
            },
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    val days = (0..8).map { LocalDate.now().plusDays(it.toLong()) }

    Row(
        modifier =
            Modifier.Companion
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = { openDatePicker.value = true }) {
            Icon(Icons.Default.DateRange, contentDescription = "Abrir calendário")
        }

        days.forEach { date ->
            FilterChip(
                selected = date == selectedDate,
                onClick = { onDateSelected(date) },
                label = {
                    Text(date.format(DateTimeFormatter.ofPattern("dd MMM")))
                },
            )
        }
    }
}
