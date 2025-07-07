package com.tasa.ui.screens.authentication.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tasa.R
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel,
    onLoginSuccess: () -> Unit,
    onNavigationBack: () -> Unit,
    onRegisterRequested: () -> Unit,
) {
    val currentState = viewModel.state.collectAsState().value
    LaunchedEffect(currentState) {
        if (currentState is LoginScreenState.Success) {
            onLoginSuccess()
        }
    }
    Scaffold(
        modifier =
            Modifier
                .fillMaxSize(),
        topBar = { TopBar(NavigationHandlers(onBackRequested = onNavigationBack)) },
    ) { innerPadding ->

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            when (currentState) {
                is LoginScreenState.Loading -> {
                    LoadingView()
                }
                is LoginScreenState.Success -> { }
                is LoginScreenState.Error -> {
                    ErrorAlert(
                        title = stringResource(R.string.error),
                        message = currentState.message,
                        buttonText = stringResource(R.string.ok),
                        onDismiss = { viewModel.setIdleState() },
                    )
                }

                LoginScreenState.Idle -> {
                    LoginView(
                        onSubmit = { username, password ->
                            viewModel.login(username, password)
                        },
                        onRegisterRequested = onRegisterRequested,
                    )
                }
            }
        }
    }
}
