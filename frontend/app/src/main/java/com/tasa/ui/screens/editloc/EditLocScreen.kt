package com.tasa.ui.screens.editloc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tasa.R
import com.tasa.domain.Location
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.screens.authentication.register.SuccessView
import com.tasa.ui.theme.TasaTheme

@Composable
fun EditLocScreen(
    onUpdateSuccessful: () -> Unit,
    onNewCenter: () -> Unit,
    location: Location,
    viewModel: EditLocScreenViewModel,
    onNavigationBack: () -> Unit,
) {
    TasaTheme {
        val currentState = viewModel.state.collectAsState().value
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
        ) { innerPaddding ->

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(innerPaddding),
            ) {
                when (currentState) {
                    is EditLocScreenState.Loading -> {
                        LoadingView()
                    }
                    is EditLocScreenState.Idle -> {
                        EditLocView(
                            location = location,
                            onSave = { name, radius, location ->
                                viewModel.editLocFields(name, radius, location)
                            },
                            onNewCenter = onNewCenter,
                        )
                    }
                    is EditLocScreenState.Success ->
                        SuccessView(
                            message = stringResource(R.string.loc_update_successful),
                            onButtonClick = {
                                onUpdateSuccessful()
                            },
                        )
                    is EditLocScreenState.Error -> {
                        ErrorAlert(
                            title = stringResource(R.string.error),
                            message = currentState.error,
                            buttonText = stringResource(R.string.Ok),
                            onDismiss = {
                                viewModel.setIdleState()
                            },
                        )
                    }
                }
            }
        }
    }
}
