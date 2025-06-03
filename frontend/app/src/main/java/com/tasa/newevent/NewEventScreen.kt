package com.tasa.newevent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tasa.authentication.register.SuccessView
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme

@Composable
fun NewEventScreen(
    viewModel: NewEventViewModel,
    onNavigationBack: () -> Unit,
    onAddedEvent: () -> Unit,
) {
    TasaTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopBar(NavigationHandlers(onBackRequested = onNavigationBack))
            },
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
            ) {
                when (val currentState = viewModel.state.collectAsState().value) {
                    is NewEventScreenState.Idle -> {
                        NewEventView(
                            onAddEvent = { event -> viewModel.addEvent(event) },
                        )
                    }
                    is NewEventScreenState.Loading -> {
                        LoadingView()
                    }
                    is NewEventScreenState.Success -> {
                        SuccessView(
                            message = "Event added successfully",
                            onButtonClick = { onAddedEvent() },
                        )
                    }
                    is NewEventScreenState.Error -> {
                        ErrorAlert(
                            title = "Add Event Error",
                            message = currentState.error.message,
                            buttonText = "Ok",
                            onDismiss = { viewModel.setIdleState() },
                        )
                    }
                }
            }
        }
    }
}
/*
@Preview(showBackground = true)
@Composable
fun NewEventScreenPreview() {
    NewEventScreen() {}
}


 */
