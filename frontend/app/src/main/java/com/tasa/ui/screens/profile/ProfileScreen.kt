package com.tasa.ui.screens.profile

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
fun ProfileScreen(
    viewModel: ProfileScreenViewModel,
    onEditAction: (Profile) -> Unit = { },
    onNavigateBack: () -> Unit,
) {
    TasaTheme {
        Scaffold(
            modifier =
                Modifier
                    .fillMaxSize(),
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
                when (val currentState = viewModel.state.collectAsState().value) {
                    is ProfileScreenState.Idle,
                    is ProfileScreenState.Loading,
                    -> {
                        LoadingView()
                    }
                    is ProfileScreenState.Success -> {
                        ProfileView(
                            state = currentState,
                            onEditUsernameClick = {
                                onEditAction(currentState.profile)
                            },
                        )
                    }
                    is ProfileScreenState.EditingUsername -> {
                       /* EditingUsernameView(
                            state = currentState,
                            onSaveIntent = { newUsername ->
                                viewModel.editUsername(newUsername)
                                // TODO side effect
                            },
                            onCancelIntent = { viewModel.setSuccessState(currentState.profile) }
                        )*/
                    }

                    is ProfileScreenState.Error -> {
                        ErrorAlert(
                            title = "Error",
                            message = currentState.error.message,
                            buttonText = "Ok",
                            onDismiss = { onNavigateBack() },
                        )
                    }
                }
            }
        }
    }
}
