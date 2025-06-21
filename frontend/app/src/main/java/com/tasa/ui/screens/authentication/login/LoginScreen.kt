package com.tasa.ui.screens.authentication.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme

@Composable
fun LoginScreen(
    viewModel: LoginScreenViewModel,
    onLoginSuccess: () -> Unit,
    onNavigationBack: () -> Unit,
    onRegisterRequested: () -> Unit,
) {
    val loginState = viewModel.state.collectAsState().value

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
                when (loginState) {
                    is LoginScreenState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is LoginScreenState.Success -> {
                        onLoginSuccess()
                    }
                    else -> {
                        LoginView(
                            onSubmit = { email, password -> viewModel.login(email, password) },
                            onRegisterRequested = onRegisterRequested,
                        )
                    }
                }
            }
        }
    }
}
