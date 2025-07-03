package com.tasa.ui.screens.mylocations

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
import com.tasa.domain.Location
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.HandleSuccessSnackbar
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar

@Composable
fun MyLocationsScreen(
    viewModel: MyLocationsScreenViewModel,
    onLocationSelected: (Location) -> Unit,
    onAddLocation: () -> Unit,
    onDeleteLocation: (Location) -> Unit,
    onEditLocation: (Location) -> Unit,
    onNavigateBack: () -> Unit,
    onCreateRuleLocationTimeless: (Location) -> Unit,
    onSetCreateRuleState: (Location) -> Unit,
    onSetSuccessState: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(NavigationHandlers(onBackRequested = onNavigateBack))
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            val state = viewModel.state.collectAsState().value
            when (state) {
                is MyLocationsScreenState.Error ->
                    ErrorAlert(
                        title = stringResource(R.string.error),
                        message = stringResource(state.resourceID),
                        buttonText = stringResource(R.string.Ok),
                        onDismiss = { onNavigateBack() },
                    )

                is MyLocationsScreenState.Uninitialized,
                is MyLocationsScreenState.Loading,
                is MyLocationsScreenState.CreatingRuleLocation,
                -> LoadingView()

                is MyLocationsScreenState.Success -> {
                    MyLocationsView(
                        locations = state.locations,
                        onEdit = { location ->
                            onEditLocation(location)
                        },
                        onDelete = {
                                location ->
                            onDeleteLocation(location)
                        },
                        onSetCreateRuleState = { location ->
                            onSetCreateRuleState(location)
                        },
                        onSetCreateTimelessRuleState = { location ->
                            onCreateRuleLocationTimeless(location)
                        },
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
