package com.tasa.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.tasa.domain.CalendarEvent
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme
import java.time.LocalDateTime

@Composable
fun CalendarScreen(
    onEventSelected: (CalendarEvent) -> Unit,
    onCreateRuleEvent: (CalendarEvent, LocalDateTime, LocalDateTime) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit = { },
    onNavigationBack: () -> Unit,
    viewModel: CalendarScreenViewModel,
) {
    TasaTheme {
        val state = viewModel.state.collectAsState().value
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    NavigationHandlers(
                        onBackRequested = if (state !is CalendarScreenState.CreatingRuleEvent) onNavigationBack else onCancel,
                    ),
                )
            },
        ) { innerPadding ->

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                when (state) {
                    is CalendarScreenState.Uninitialized,
                    is CalendarScreenState.Loading,
                    -> {
                        LoadingView()
                    }

                    is CalendarScreenState.Error -> {
                        ErrorAlert(
                            title = "Error",
                            message = state.message,
                            buttonText = "Dismiss",
                            onDismiss = { onNavigationBack() },
                        )
                    }

                    is CalendarScreenState.SuccessOnSchedule -> {
                        CalendarView(
                            onEventSelected = { calendarEvent -> onEventSelected(calendarEvent) },
                            eventsFlow = state.events,
                            successOnSchedule = true,
                        )
                    }
                    is CalendarScreenState.CreatingRuleEvent -> {
                        CreateRulEventView(
                            event = state.event,
                            onCreate = { calendarEvent, startTime, endTime ->
                                onCreateRuleEvent(calendarEvent, startTime, endTime)
                            },
                            onCancel = { onCancel() },
                        )
                    }
                    is CalendarScreenState.Success -> {
                        CalendarView(
                            onEventSelected = { calendarEvent -> onEventSelected(calendarEvent) },
                            eventsFlow = state.events,
                        )
                    }
                }
            }
        }
    }
}
