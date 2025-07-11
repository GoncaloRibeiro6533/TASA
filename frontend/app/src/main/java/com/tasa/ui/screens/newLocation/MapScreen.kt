package com.tasa.ui.screens.newLocation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import com.tasa.R
import com.tasa.domain.Location
import com.tasa.ui.components.ErrorAlert
import com.tasa.ui.components.LoadingView
import com.tasa.ui.components.NavigationHandlers
import com.tasa.ui.components.TopBar
import com.tasa.ui.screens.newLocation.mapViewStates.CreatingLocationView
import com.tasa.ui.screens.newLocation.mapViewStates.MapView
import com.tasa.ui.screens.newLocation.mapViewStates.MapViewEditLocationView
import com.tasa.ui.screens.newLocation.mapViewStates.MapViewSearching
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
    onRecenterMap: () -> Unit,
    onLocationsIntent: () -> Unit,
    onSessionExpired: () -> Unit,
    previousLocation: Location,
    onEditCenterButton: (
        Location,
        String,
        Double,
        Double,
        Double
    ) -> Unit,
    onConfirmEditingLocation: (
        String,
        Double,
        Double,
        Double
    ) -> Unit,
) {
    val state = viewModel.state.collectAsState().value
    LaunchedEffect(state) {
        if (state is MapsScreenState.SuccessCreatingLocation) {
            onLocationsIntent()
        }
    }
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
            when (state) {
                is MapsScreenState.Uninitialized,
                is MapsScreenState.Loading,
                -> {
                    LoadingView()
                }
                is MapsScreenState.Success,
                is MapsScreenState.EditingLocation,
                is MapsScreenState.SuccessSearching,
                is MapsScreenState.ChangingCenter,
                -> {
                    MapViewRoot(
                        locationV = viewModel.currentLocation,
                        radius =
                            if (state is MapsScreenState.EditingLocation) {
                                viewModel.radius
                            } else {
                                null
                            },
                        selectedPoint = viewModel.selectedPoint,
                        onLocationSelected = onLocationSelected,
                    ) {
                        when (state) {
                            is MapsScreenState.Success -> {
                                MapView(
                                    onSearchQuery = onSearchQuery,
                                    onEditSearchBox = onEditSearchBox,
                                    onCreateLocation = onCreateLocationButton,
                                    query = state.searchQuery,
                                    onRecenterMap = onRecenterMap,
                                )
                            }
                            is MapsScreenState.EditingLocation -> {
                                CreatingLocationView(
                                    location = state.currentLocation,
                                    selectedPoint = state.selectedPoint,
                                    locationName = state.locationName,
                                    radius = state.radius,
                                    onDismiss = onDismissEditingLocation,
                                    onConfirm = onConfirmEditingLocation,
                                    onChangeRadius = onUpdateRadius,
                                    onChangeLocationName = onUpdateLocationName,
                                )
                            }
                            is MapsScreenState.SuccessSearching -> {
                                MapViewSearching(
                                    query = state.searchQuery,
                                    onSearch = onSearchQuery,
                                    onWriteSearchBox = onEditSearchBox,
                                    onCreateLocation = onCreateLocationButton,
                                    onRecenterMap = onRecenterMap,
                                )
                            }
                            is MapsScreenState.ChangingCenter -> {
                                MapViewEditLocationView(
                                    previousLocation = previousLocation,
                                    location = state.currentLocation,
                                    selectedPoint = state.selectedPoint,
                                    locationName = state.locationName,
                                    radius = state.radius,
                                    onDismiss = onDismissEditingLocation,
                                    onConfirm = onEditCenterButton,
                                    onChangeRadius = onUpdateRadius,
                                    onChangeLocationName = onUpdateLocationName,
                                )
                            }

                            else -> {
                                // Do nothing, handled in the outer scope
                            }
                        }
                    }
                }
                is MapsScreenState.Error -> {
                    ErrorAlert(
                        title = stringResource(R.string.error),
                        message = state.error,
                        buttonText = stringResource(R.string.Ok),
                    ) {
                        onNavigationBack()
                    }
                }
                is MapsScreenState.SuccessCreatingLocation -> {
                }
                is MapsScreenState.SessionExpired -> {
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
