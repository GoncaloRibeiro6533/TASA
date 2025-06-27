package com.tasa.ui.screens.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme

@Composable
fun StartScreen(
    viewModel: StartScreenViewModel,
    onAboutRequested: () -> Unit,
    onLoginRequested: () -> Unit = { },
    onRegisterRequested: () -> Unit = { },
    onLoggedIntent: () -> Unit = { },
) {
    TasaTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopBar(
                    NavigationHandlers(
                      /*  onAboutRequested = onAboutRequested,*/
                    ),
                )
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
                when (viewModel.state) {
                    StartScreenState.Idle -> {
                        HomePopPup()
                    }
                    StartScreenState.Logged -> {
                        onLoggedIntent()
                    }
                    StartScreenState.NotLogged -> {
                        StartView(
                            onLoginRequested = onLoginRequested,
                            onRegisterRequested = onRegisterRequested,
                            onAboutRequested = onAboutRequested,
                        )
                    }
                }
            }
        }
    }
}
