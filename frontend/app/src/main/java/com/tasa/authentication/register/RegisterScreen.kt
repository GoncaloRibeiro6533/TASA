package com.tasa.authentication.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme

@Composable
fun RegisterScreen(
    viewModel: RegisterScreenViewModel,
    onRegisterSuccessful: () -> Unit,
    onNavigationBack: () -> Unit = { }) {
    TasaTheme {
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
                when(val currentState = viewModel.state.collectAsState().value) {
                    is RegisterScreenState.Idle -> {
                        RegisterView(
                            onSubmit = { email, username, password ->
                                viewModel.registerUser(email, username, password) },
                        )
                    }
                    is RegisterScreenState.Loading -> {
                        LoadingView()
                    }
                    is RegisterScreenState.Success -> {
                        SuccessView(
                            message = "User registered successfully",
                            onButtonClick = {onRegisterSuccessful()}
                        )
                    }
                    is RegisterScreenState.Error -> {
                        ErrorAlert(
                            title = "Error",
                            message = currentState.error.message,
                            buttonText = "Ok",
                            onDismiss = { viewModel.setIdleState() }
                        )
                    }
                }

            }
        }
    }
}
