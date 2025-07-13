package com.tasa.ui.screens.editloc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.tasa.ui.screens.editloc.editview.MapViewEditLocationView
import com.tasa.ui.screens.editloc.editview.MapViewRootEditLocation
import com.tasa.ui.screens.newLocation.MapViewRoot
import com.tasa.ui.theme.TasaTheme
import org.osmdroid.util.GeoPoint

@Composable
fun EditLocScreen(
    onUpdateSuccessful: () -> Unit,
    location: Location,
    viewModel: EditLocScreenViewModel,
    onSessionExpired: () -> Unit,
    onNavigationBack: () -> Unit,
    onLocationSelected: (GeoPoint) -> Unit,
    onDismissChangingCenter: () -> Unit,
    onEditCenterButton: (Location, String, Double, Double, Double) -> Unit,
    onUpdateRadius: (Double) -> Unit,
    onUpdateLocationName: (String) -> Unit,


    ) {
    TasaTheme {
        val currentState = viewModel.state.collectAsState().value

        LaunchedEffect(Unit) {
            if (currentState is EditLocScreenState.Uninitialized) {
                viewModel.initializeEditing(location)
            }
        }


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
                            onSave = { location, name, radius ->
                                viewModel.onChangeLocationFields(location, name, radius)
                            },
                            onNewCenter = {
                                viewModel.setEditingCenterState()
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
                    is EditLocScreenState.ChangingCenter -> {
                        viewModel.updateLocation(location)


                        MapViewRootEditLocation(
                            radius = viewModel.radius,
                            selectedPoint = viewModel.selectedPoint,
                            onLocationSelected = onLocationSelected,
                        ) {
                            val name = location.name
                            val radius = location.radius
                            viewModel.updateRadius(radius)
                            viewModel.editLocationName(name)

                            val latitude = location.latitude
                            val longitude = location.longitude
                            val geoPoint = GeoPoint(latitude, longitude)
                            viewModel.updateSelectedPoint(geoPoint)
                            viewModel.setEditingCenterState()
                            println("loc1 lat:$latitude lon:$longitude")

                            MapViewEditLocationView(
                                previousLocation = location,
                                selectedPoint = currentState.selectedPoint,
                                locationName = currentState.locationName,
                                radius = currentState.radius,
                                onDismiss = onDismissChangingCenter,
                                onConfirm = onEditCenterButton,
                                onChangeRadius = onUpdateRadius,
                                onChangeLocationName = onUpdateLocationName,
                            )
                        }
                    }
                }
            }
        }
    }
}
