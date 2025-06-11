package com.tasa.mylocations.components

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
import com.tasa.mylocations.MyLocationsScreenState
import com.tasa.mylocations.MyLocationsScreenViewModel
import com.tasa.mylocations.MyLocationsView
import com.tasa.ui.components.ErrorAlert
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
) {
    Scaffold(
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
            when (val state = viewModel.state.collectAsState().value) {
                is MyLocationsScreenState.Error ->
                    ErrorAlert(
                        title = stringResource(R.string.error),
                        message = state.message,
                        buttonText = stringResource(R.string.ok),
                        onDismiss = { onNavigateBack() },
                    )

                is MyLocationsScreenState.Uninitialized,
                MyLocationsScreenState.Loading,
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
                    )
                }
            }
        }
    }
}
