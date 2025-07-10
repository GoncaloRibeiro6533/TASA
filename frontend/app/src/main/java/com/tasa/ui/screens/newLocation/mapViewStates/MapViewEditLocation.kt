package com.tasa.ui.screens.newLocation.mapViewStates

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.domain.Location
import com.tasa.ui.screens.newLocation.TasaLocation
import kotlinx.coroutines.flow.StateFlow
import org.osmdroid.util.GeoPoint


@Composable
fun MapViewEditLocationView(
    previousLocation: Location,
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