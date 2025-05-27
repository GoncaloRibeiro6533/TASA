package com.tasa.homepage

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
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.theme.TasaTheme

@Composable
fun HomePageScreen(
    viewModel: HomePageScreenViewModel,
    onNavigationToMap: () -> Unit,
    onNavigateToCreateRuleEvent: () -> Unit = {},
    onNavigateToMyExceptions: () -> Unit = {},
    onMenuRequested: () -> Unit = { },
    onFatalError: () -> Unit = { },
) {
    TasaTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
            topBar = {
                TopBar(NavigationHandlers(onMenuRequested = onMenuRequested))
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
                when (val state = viewModel.state.collectAsState().value) {
                    is HomeScreenState.Error -> {
                        ErrorAlert(
                            title = "Error",
                            message = state.error.message,
                            buttonText = "Close app",
                            onDismiss = { onFatalError() },
                        )
                    }
                    HomeScreenState.Loading -> LoadingView()
                    is HomeScreenState.Success ->
                        HomePageView(
                            rules = state.rules,
                            onNavigationToMap = onNavigationToMap,
                            onNavigateToCreateRuleEvent = onNavigateToCreateRuleEvent,
                            onNavigationToMyExceptions = onNavigateToMyExceptions,
                        )
                    HomeScreenState.Uninitialized -> { // Do nothing}
                    }
                }
            }
        }
    }
}
