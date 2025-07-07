package com.tasa.ui.screens.authentication.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tasa.R
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar

@Composable
fun RegisterScreen(
    viewModel: RegisterScreenViewModel,
    onSubmit: (String, String, String) -> Unit,
    onRegisterSuccessful: () -> Unit,
    onNavigationBack: () -> Unit = { },
) {
    Scaffold(
        modifier =
            Modifier
                .fillMaxSize(),
        topBar = {
            TopBar(
                NavigationHandlers(
                    onBackRequested = {
                        onNavigationBack()
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
            when (val currentState = viewModel.state.collectAsState().value) {
                is RegisterScreenState.Idle -> {
                    RegisterView(
                        onSubmit = { email, username, password ->
                            onSubmit(email, username, password)
                        },
                    )
                }
                is RegisterScreenState.Loading -> {
                    LoadingView()
                }
                is RegisterScreenState.Success -> {
                    SuccessView(
                        message = stringResource(R.string.registration_successful),
                        onButtonClick = { onRegisterSuccessful() },
                    )
                }
                is RegisterScreenState.Error -> {
                    ErrorAlert(
                        title = stringResource(R.string.error),
                        message = currentState.error.message,
                        buttonText = stringResource(R.string.Ok),
                        onDismiss = { viewModel.setIdleState() },
                    )
                }
            }
        }
    }
}
