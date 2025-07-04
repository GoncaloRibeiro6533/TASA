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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.ui.screens.newLocation.components.OSMDroidMap2
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

@Composable
fun MapView(
    onEditSearchBox: (TextFieldValue) -> Unit,
    onCreateLocation: () -> Unit,
    onSearchQuery: () -> Unit,
    query: StateFlow<TextFieldValue>,
    onRecenterMap: () -> Unit,
) {
    SearchBox(
        query = query.collectAsState().value,
        onQueryChange = { newValue ->
            onEditSearchBox(newValue)
        },
        onSearch = {
            onSearchQuery()
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            onClick = {
                onRecenterMap()
            },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_recenter_icon),
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
    query: StateFlow<TextFieldValue>,
    onSearch: () -> Unit,
    onWriteSearchBox: (TextFieldValue) -> Unit,
    onCreateLocation: () -> Unit,
    onRecenterMap: () -> Unit,
) {
    var searchQuery = query.collectAsState().value
    SearchBox(
        query = searchQuery,
        onQueryChange = { newValue ->
            onWriteSearchBox(newValue)
        },
        onSearch = {
            onSearch()
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        FloatingActionButton(
            onClick = {
                onRecenterMap()
            },
            modifier =
                Modifier
                    .padding(16.dp),
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_recenter_icon),
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
}

val coral = Color(0xFFFF7F50) // Cor base quente e vibrante

@Composable
fun CreatingLocationView(
    location: StateFlow<TasaLocation>,
    selectedPoint: StateFlow<GeoPoint?>,
    locationName: StateFlow<String>,
    radius: StateFlow<Double>,
    onDismiss: () -> Unit,
    onChangeLocationName: (String) -> Unit,
    onChangeRadius: (Double) -> Unit,
    onConfirm: (String, Double, Double, Double) -> Unit,
) {
    val selectedPoint = selectedPoint.collectAsState().value
    val location = location.collectAsState().value
    val locationName = locationName.collectAsState().value
    val radius = radius.collectAsState().value
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
                Text(stringResource(R.string.create_location))
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { onChangeLocationName(it) },
                    label = { Text(stringResource(R.string.name)) },
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
                            thumbColor = coral,
                            activeTrackColor = coral.copy(alpha = 0.4f),
                            inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            activeTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            inactiveTickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.radius) + ": " + radius.toInt() + " " + stringResource(R.string.meters),
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(onClick = { onDismiss() }) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        enabled = locationName.isNotBlank() && radius > 0,
                        onClick = {
                            onConfirm(
                                locationName,
                                radius,
                                selectedPoint?.latitude ?: location.point.latitude,
                                selectedPoint?.longitude ?: location.point.longitude,
                            )
                        },
                    ) {
                        Text(stringResource(R.string.create))
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

@Composable
fun SearchBox(
    query: TextFieldValue,
    onQueryChange: (TextFieldValue) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                value = query,
                onValueChange = { newValue ->
                    onQueryChange(newValue)
                },
                label = { Text(stringResource(R.string.search_for_a_place)) },
                modifier = modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    onSearch()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.search))
            }
        }
    }
}

/*){
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
        Text(stringResource(R.string.search))
    }
}*/

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
