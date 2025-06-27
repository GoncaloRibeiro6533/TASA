package com.tasa.ui.screens.newLocation

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
import androidx.compose.ui.text.input.TextFieldValue
import com.tasa.R
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import org.osmdroid.util.GeoPoint

@Composable
fun MapScreen(
    viewModel: MapScreenViewModel,
    onNavigationBack: () -> Unit,
    onLocationSelected: (GeoPoint) -> Unit,
    onSearchQuery: () -> Unit,
    onUpdateRadius: (Double) -> Unit,
    onUpdateLocationName: (String) -> Unit,
    onEditSearchBox: (TextFieldValue) -> Unit,
    onCreateLocationButton: () -> Unit,
    onDismissEditingLocation: () -> Unit,
    onConfirmEditingLocation: (String, Double, Double, Double) -> Unit,
    onTouchSearchBox: () -> Unit,
    onUnTouchSearchBox: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(NavigationHandlers(onBackRequested = onNavigationBack))
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
                is MapsScreenState.Uninitialized,
                is MapsScreenState.Loading,
                -> {
                    LoadingView()
                }
                is MapsScreenState.Success,
                is MapsScreenState.EditingLocation,
                is MapsScreenState.SuccessSearching,
                -> {
                    MapViewRoot(
                        state = viewModel.state,
                        onLocationSelected = onLocationSelected,
                        onEditSearchBox = { it -> onEditSearchBox(it) },
                        onCreateLocation = onCreateLocationButton,
                        onSearch = onSearchQuery,
                        onWriteSearchBox = { it ->
                            onEditSearchBox(it)
                        },
                        onDismiss = onDismissEditingLocation,
                        onChangeLocationName = { it ->
                            onUpdateLocationName(it)
                        },
                        onChangeRadius = { radius ->
                            onUpdateRadius(radius)
                        },
                        onConfirm = onConfirmEditingLocation,
                        onTouchSearchBox = onTouchSearchBox,
                        onUnTouchSearchBox = onUnTouchSearchBox,
                    )
                }
                is MapsScreenState.Error -> {
                    ErrorAlert(
                        title = stringResource(R.string.error),
                        message = state.message,
                        buttonText = stringResource(R.string.Ok),
                    ) {
                        onNavigationBack()
                    }
                }
            }
        }
    }
}
