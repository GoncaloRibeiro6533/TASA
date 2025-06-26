package com.tasa.ui.screens.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.domain.CalendarEvent
import com.tasa.ui.screens.calendar.components.CalendarEventCard
import com.tasa.ui.screens.calendar.components.DateSelectorBar
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val CALENDAR_VIEW = "calendar_view"
const val LOGIN_TEXT_FIELDS = "login_text_fields"
const val LOGIN_BUTTON = "login_button"
const val REGISTER_ANCHOR = "register_anchor"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarView(
    onEventSelected: (CalendarEvent) -> Unit,
    eventsFlow: StateFlow<List<CalendarEvent>>,
    successOnSchedule: Boolean = false,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val events by eventsFlow.collectAsState()
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }

    val filteredEvents =
        rememberSaveable(events, selectedDate) {
            events.filter { it.startTime.toLocalDate() == selectedDate }
        }
    val message = stringResource(R.string.rule_scheduled_success)
    LaunchedEffect(successOnSchedule) {
        if (successOnSchedule) {
            snackbarHostState.showSnackbar(message)
            delay(5000)
            snackbarHostState.currentSnackbarData?.dismiss()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, modifier = Modifier.padding(16.dp)) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag(CALENDAR_VIEW),
        ) {
            DateSelectorBar(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                stringResource(R.string.events_in) + " ${selectedDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredEvents.isEmpty()) {
                Text(stringResource(R.string.no_event_found))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filteredEvents) { event ->
                        CalendarEventCard(
                            event = event,
                            onSelected = {
                                onEventSelected(it)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalendarViewPreview() {
    val sampleEvents =
        listOf(
            CalendarEvent(
                eventId = 1L,
                calendarId = 1L,
                title = "Evento 1",
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusHours(1),
            ),
            CalendarEvent(
                eventId = 2L,
                calendarId = 1L,
                title = "Evento 2",
                startTime = LocalDateTime.now().plusDays(1),
                endTime = LocalDateTime.now().plusDays(1).plusHours(2),
            ),
        )

    CalendarView(
        onEventSelected = {},
        eventsFlow = MutableStateFlow(sampleEvents),
    )
}
