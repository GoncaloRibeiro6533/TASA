package com.tasa.ui.screens.newLocation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.tasa.ui.screens.newLocation.components.OSMDroidMap2
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint

const val OSMDROID_MAP = "osmdroid_map"
const val LOGIN_TEXT_FIELDS = "login_text_fields"
const val LOGIN_BUTTON = "login_button"
const val REGISTER_ANCHOR = "register_anchor"


@Composable
fun MapViewRoot(
    state: StateFlow<MapsScreenState>,
    // Actions
    onLocationSelected: (GeoPoint) -> Unit,
    onEditSearchBox: (TextFieldValue) -> Unit,
    onCreateLocation: () -> Unit,
    onSearch: () -> Unit,
    onWriteSearchBox: (TextFieldValue) -> Unit,
    onDismiss: () -> Unit,
    onChangeLocationName: (String) -> Unit,
    onChangeRadius: (Double) -> Unit,
    onConfirm: (String, Double, Double, Double) -> Unit,
    onTouchSearchBox: () -> Unit,
    onUnTouchSearchBox: () -> Unit,
) {
    val currentState = state.collectAsState().value
    var locationV: TasaLocation =
        when (currentState) {
            is MapsScreenState.Success -> currentState.currentLocation.collectAsState().value
            is MapsScreenState.EditingLocation -> currentState.currentLocation.collectAsState().value
            is MapsScreenState.SuccessSearching -> currentState.currentLocation.collectAsState().value
            else ->
                TasaLocation(
                    point = GeoPoint(38.7169, -9.1399),
                    accuracy = 10f,
                    updates = 0,
                )
        }
    var radius =
        when (currentState) {
            is MapsScreenState.EditingLocation -> currentState.radius.collectAsState().value
            else -> null // Default radius if not editing
        }
    var selectedPoint =
        when (currentState) {
            is MapsScreenState.Success -> currentState.selectedPoint.collectAsState().value
            is MapsScreenState.EditingLocation -> currentState.selectedPoint.collectAsState().value
            is MapsScreenState.SuccessSearching -> currentState.selectedPoint.collectAsState().value
            else -> null // Default selected point if not in a valid state
        }
    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        // Map in the background
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
        when (currentState) {
            is MapsScreenState.Success -> {
                MapView(
                    location = currentState.currentLocation,
                    onLocationSelected = onLocationSelected,
                    selectedPoint = currentState.selectedPoint,
                    activity = currentState.userActivity,
                    onEditSearchBox = onEditSearchBox,
                    onCreateLocation = onCreateLocation,
                    onSearchQuery = onSearch,
                    query = currentState.searchQuery,
                )
            }

            is MapsScreenState.SuccessSearching -> {
                MapViewSearching(
                    location = currentState.currentLocation,
                    onLocationSelected = onLocationSelected,
                    selectedPoint = currentState.selectedPoint,
                    activity = currentState.userActivity,
                    query = currentState.searchQuery,
                    onSearch = onSearch,
                    onWriteSearchBox = onWriteSearchBox,
                    onCreateLocation = onCreateLocation,
                    onTouchSearchBox = onTouchSearchBox,
                    onUnTouchSearchBox = onUnTouchSearchBox,
                    state = state,
                )
            }

            is MapsScreenState.EditingLocation -> {
                CreatingLocationView(
                    location = currentState.currentLocation,
                    onLocationSelected = onLocationSelected,
                    selectedPoint = currentState.selectedPoint,
                    onEditSearchBox = onEditSearchBox,
                    activity = currentState.userActivity,
                    query = currentState.searchQuery,
                    locationName = currentState.locationName,
                    radius = currentState.radius,
                    onDismiss = onDismiss,
                    onChangeLocationName = onChangeLocationName,
                    onChangeRadius = onChangeRadius,
                    onConfirm = onConfirm,
                    state = state,
                )
            }

            else -> {}
        }
    }
}

@Composable
fun MapView(
    location: StateFlow<TasaLocation>,
    onLocationSelected: (GeoPoint) -> Unit,
    selectedPoint: StateFlow<GeoPoint?>,
    activity: StateFlow<String?>,
    onEditSearchBox: (TextFieldValue) -> Unit,
    onCreateLocation: () -> Unit,
    onSearchQuery: () -> Unit,
    query: StateFlow<TextFieldValue>,
) {
    val selectedPoint = selectedPoint.collectAsState().value
    val location = location.collectAsState().value
    val activity = activity.collectAsState().value

    /* Box(
         modifier =
             Modifier
                 .fillMaxSize(),
     ) {
         // Map in the background
         OSMDroidMap2(
             modifier = Modifier.fillMaxSize(),
             center = location.point,
             currentLocation = location.point,
             onCoordinateSelected = { point ->
                 onLocationSelected(point)
             },
             accuracy = location.accuracy,
             onMapReady = onMapReady,
             radius = null,
         )*/
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            OutlinedTextField(
                value = query.collectAsState().value,
                onValueChange = { onEditSearchBox(it) },
                label = { Text("Search for a place") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSearchQuery()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Search")
            }
            Text(
                text = "Current accuracy: ${location.accuracy?.toInt()} meters",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Updates: ${location.updates}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Latitude: ${location.point.latitude}, Longitude:${location.point.longitude}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Atividade: $activity",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            onClick = { },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Atualizar localização",
            )
        }
        FloatingActionButton(
            onClick = { onCreateLocation() },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Criar localização",
            )
        }
    }
    // }
}

@Composable
fun MapViewSearching(
    state: StateFlow<MapsScreenState>,
    location: StateFlow<TasaLocation>,
    onLocationSelected: (GeoPoint) -> Unit,
    selectedPoint: StateFlow<GeoPoint?>,
    activity: StateFlow<String?>,
    query: StateFlow<TextFieldValue>,
    onSearch: () -> Unit,
    onWriteSearchBox: (TextFieldValue) -> Unit,
    onCreateLocation: () -> Unit,
    onTouchSearchBox: () -> Unit,
    onUnTouchSearchBox: () -> Unit,
) {
    val selectedPoint = selectedPoint.collectAsState().value
    var searchQuery = query.collectAsState().value
    val location = location.collectAsState().value
    val focusManager = LocalFocusManager.current
    val activity = activity.collectAsState().value

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            /*OutlinedTextField(
                value = searchQuery,
                onValueChange = { onWriteSearchBox(it) },
                label = { Text("Search for a place") },
                modifier = Modifier.fillMaxWidth()
            )*/
            PersistentFocusOutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue ->
                    onWriteSearchBox(newValue)
                },
                label = "Search for a place",
                state = state,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSearch()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Search")
            }
            Text(
                text = "Current accuracy: ${location.accuracy?.toInt()} meters",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Updates: ${location.updates}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Latitude: ${location.point.latitude}, Longitude:${location.point.longitude}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            /*Text(
                text = "Atividade: $activity",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )*/
        }
    }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            onClick = { },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Atualizar localização",
            )
        }
        FloatingActionButton(
            onClick = { onCreateLocation() },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Criar localização",
            )
        }
    }
    // }
}

@Composable
fun CreatingLocationView(
    state: StateFlow<MapsScreenState>,
    location: StateFlow<TasaLocation>,
    onLocationSelected: (GeoPoint) -> Unit,
    selectedPoint: StateFlow<GeoPoint?>,
    onEditSearchBox: (TextFieldValue) -> Unit,
    activity: StateFlow<String?>,
    query: StateFlow<TextFieldValue>,
    locationName: StateFlow<String>,
    radius: StateFlow<Double>,
    onDismiss: () -> Unit,
    onChangeLocationName: (String) -> Unit,
    onChangeRadius: (Double) -> Unit,
    onConfirm: (String, Double, Double, Double) -> Unit,
) {
    val selectedPoint = selectedPoint.collectAsState().value
    var searchQuery = query.collectAsState().value
    val location = location.collectAsState().value
    val locationName = locationName.collectAsState().value
    val radius = radius.collectAsState().value
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            PersistentFocusOutlinedTextField(
                value = searchQuery,
                onValueChange = { onEditSearchBox(it) },
                label = "Search for a place",
                modifier = Modifier.fillMaxWidth(),
                state = state,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Search")
            }
            Text(
                text = "Current accuracy: ${location.accuracy?.toInt()} meters",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Updates: ${location.updates}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = "Latitude: ${location.point.latitude}, Longitude:${location.point.longitude}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            /*Text(
                text = "Atividade: $activity",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )*/
        }
    }
    // 📝 Dialog with form
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            modifier = Modifier.width(280.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                Text("Enter info for this location:")
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { onChangeLocationName(it) },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Slider(
                    value = radius.toFloat(),
                    onValueChange = {
                        onChangeRadius(it.toDouble())
                    },
                    valueRange = 10f..50f,
                    // Number of discrete steps
                    colors =
                        SliderDefaults.colors(
                            thumbColor = Color(0xFFFF9800),
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            activeTickColor = MaterialTheme.colorScheme.onPrimary,
                            inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Radius: ${radius.toInt()} meters",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            onConfirm(
                                locationName,
                                radius,
                                selectedPoint?.latitude ?: location.point.latitude,
                                selectedPoint?.longitude ?: location.point.longitude,
                            )
                        },
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@Composable
fun PersistentFocusOutlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    state: StateFlow<MapsScreenState>,
) {
    val focusRequester = remember { FocusRequester() }

    OutlinedTextField(
        value = value,
        onValueChange = { updated ->
            onValueChange(updated)
        },
        label = { Text(label) },
        modifier =
            modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        singleLine = true,
    )

    LaunchedEffect(Unit) {
        if (state.value is MapsScreenState.SuccessSearching) {
            focusRequester.requestFocus()
        }
    }
}
