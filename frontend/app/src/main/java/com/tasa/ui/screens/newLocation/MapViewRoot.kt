package com.tasa.ui.screens.newLocation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.tasa.ui.screens.newLocation.components.OSMDroidMap2
import com.tasa.ui.screens.newLocation.mapViewStates.MapView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

const val OSMDROID_MAP = "osmdroid_map"
const val LOGIN_TEXT_FIELDS = "login_text_fields"
const val LOGIN_BUTTON = "login_button"
const val REGISTER_ANCHOR = "register_anchor"

@Composable
fun MapViewRoot(
    locationV: StateFlow<TasaLocation>,
    radius: StateFlow<Double>? = null,
    selectedPoint: StateFlow<GeoPoint?>? = null,
    onLocationSelected: (GeoPoint) -> Unit,
    composable: @Composable () -> Unit,
) {
    val locationV = locationV.collectAsState().value
    val radius = radius?.collectAsState()?.value
    val selectedPoint = selectedPoint?.collectAsState()?.value
    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        OSMDroidMap2(
            modifier = Modifier.fillMaxSize().testTag(OSMDROID_MAP),
            center = locationV.point,
            currentLocation = locationV.point,
            onCoordinateSelected = { point ->
                onLocationSelected(point)
            },
            accuracy = locationV.accuracy,
            radius = radius,
            selectedPoint = selectedPoint,
        )
        composable()
    }
}

@Preview(showBackground = true)
@Composable
fun MapRootPreview() {
    val location =
        TasaLocation(
            point = GeoPoint(0.0, 0.0),
            accuracy = 0.0f,
            altitude = null,
        )
    val radius = MutableStateFlow(20.0)
    val selectedPoint = MutableStateFlow(GeoPoint(1.0, 1.0))
    val locationFlow = MutableStateFlow(location)
    MapViewRoot(
        locationV = locationFlow,
        radius = radius,
        selectedPoint = selectedPoint,
        onLocationSelected = {},
    ) {
        MapView(
            onEditSearchBox = {},
            onCreateLocation = {},
            onSearchQuery = {},
            query = MutableStateFlow(TextFieldValue("")),
            onRecenterMap = {},
        )
    }
}
