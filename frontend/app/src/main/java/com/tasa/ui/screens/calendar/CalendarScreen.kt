package com.tasa.ui.screens.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tasa.R
import com.tasa.domain.CalendarEvent
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.HandleSuccessSnackbar
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun CalendarScreen(
    onEventSelected: (CalendarEvent) -> Unit,
    onCreateRuleEvent: (CalendarEvent, LocalDateTime, LocalDateTime) -> Unit = { _, _, _ -> },
    onDateSelected: (LocalDate) -> Unit,
    onCancel: () -> Unit = { },
    onNavigationBack: () -> Unit,
    viewModel: CalendarScreenViewModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    TasaTheme {
        val state = viewModel.state.collectAsState().value
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopBar(
                    NavigationHandlers(
                        onBackRequested =
                            if (state !is CalendarScreenState.CreatingRuleEvent) {
                                onNavigationBack
                            } else {
                                onCancel
                            },
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
                            title = stringResource(R.string.error),
                            message = state.message,
                            buttonText = stringResource(R.string.Ok),
                            onDismiss = { onNavigationBack() },
                        )
                    }
                    is CalendarScreenState.CreatingRuleEvent -> {
                        CreateRuleEventView(
                            event = state.event,
                            onCreate = onCreateRuleEvent,
                            onCancel = onCancel,
                        )
                    }
                    is CalendarScreenState.SuccessOnSchedule,
                    is CalendarScreenState.Success,
                    -> {
                        CalendarView(
                            onEventSelected = { calendarEvent -> onEventSelected(calendarEvent) },
                            eventsFlow = viewModel.events,
                            onDateSelected = { date -> onDateSelected(date) },
                            selectedDay = viewModel.selectedDay,
                        )
                    }
                }
                HandleSuccessSnackbar(
                    snackbarHostState = snackbarHostState,
                    messageFlow = viewModel.successMessage,
                    onMessageConsumed = viewModel::clearMessageOfSuccess,
                )
            }
        }
    }
}
