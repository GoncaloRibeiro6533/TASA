package com.tasa.ui.screens.homepage

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
import androidx.compose.ui.res.stringResource
import com.tasa.R
import com.tasa.domain.Rule
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.screens.rule.EditRuleActivity
import com.tasa.ui.theme.TasaTheme

@Composable
fun HomePageScreen(
    viewModel: HomePageScreenViewModel,
    onNavigateToMyLocations: () -> Unit,
    onNavigationToMap: () -> Unit,
    onNavigateToCreateRuleEvent: () -> Unit = {},
    onNavigateToMyExceptions: () -> Unit = {},
    onMenuRequested: () -> Unit = { },
    onFatalError: () -> Unit = { },
    onEditRule: (EditRuleActivity.RuleParcelableEvent) -> Unit = {},
    onCancelRule: (Rule) -> Unit = { },
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
                            title = stringResource(R.string.error),
                            message = stringResource(state.error),
                            buttonText = stringResource(R.string.Ok),
                            onDismiss = { onFatalError() },
                        )
                    }
                    HomeScreenState.Loading -> LoadingView()
                    is HomeScreenState.Success ->
                        HomePageView(
                            rules = state.rules,
                            onNavigateToMyLocations = onNavigateToMyLocations,
                            onNavigationToMap = onNavigationToMap,
                            onNavigateToCreateRuleEvent = onNavigateToCreateRuleEvent,
                            onNavigationToMyExceptions = onNavigateToMyExceptions,
                            onEdit = onEditRule,
                            onDelete = onCancelRule,
                        )
                    HomeScreenState.Uninitialized -> { // Do nothing}
                    }
                }
            }
        }
    }
}
