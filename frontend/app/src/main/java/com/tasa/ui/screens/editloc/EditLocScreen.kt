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
import com.tasa.ui.screens.newLocation.MapsScreenState
import com.tasa.ui.theme.TasaTheme

@Composable
fun EditLocScreen(
    onUpdateSuccessful: () -> Unit,
    onNewCenter: (Location) -> Unit,
    location: Location,
    viewModel: EditLocScreenViewModel,
    onSessionExpired: () -> Unit,
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

                    is EditLocScreenState.Uninitialized,

                    is EditLocScreenState.Loading -> {
                        LoadingView()
                    }
                    is EditLocScreenState.ChangingFields -> {
                        EditLocView(
                            location = location,
                            onSave = { location, name, radius,  ->
                                viewModel.onChangeLocationFields(location, name, radius, )
                            },
                            onNewCenter = {
                                onNewCenter(location)
                            },
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
                        ){
                            onNavigationBack()
                        }
                    }

                    is EditLocScreenState.SessionExpired -> {
                        ErrorAlert(
                            title = stringResource(R.string.error),
                            message = stringResource(R.string.session_expired),
                            buttonText = stringResource(R.string.log_in),
                        ) {
                            viewModel.onFatalError()?.invokeOnCompletion { onSessionExpired() }
                        }
                    }
                }
            }
        }
    }
}
