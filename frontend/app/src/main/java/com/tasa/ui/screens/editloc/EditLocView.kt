package com.tasa.ui.screens.editloc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.domain.Location

@Composable
fun EditLocView(
    location: Location,
    onSave: (
        name: String,
        radius: Double,
        location: Location,
    ) -> Unit,
    onNewCenter: () -> Unit,
) {
    val initialName = location.name
    val initialRadius = location.radius.toString()
    var name by remember { mutableStateOf(initialName) }
    var radiusText by remember { mutableStateOf(initialRadius) }
    var radiusError by remember { mutableStateOf(false) }

    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = {
                Text(
                    text = stringResource(R.string.name),
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = radiusText,
            onValueChange = {
                radiusText = it
                radiusError = it.toDoubleOrNull() == null
            },
            label = {
                Text(
                    text = stringResource(R.string.radius_meters),
                )
            },
            isError = radiusError,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        if (radiusError) {
            Text(
                text = stringResource(R.string.valid_number),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(end = 4.dp),
                onClick = {
                    onNewCenter()
                },
            ) {
                Text(
                    text = stringResource(R.string.change_center),
                )
            }

            Button(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                onClick = {
                    onNewCenter()
                },
            ) {
                Text(
                    text = stringResource(R.string.edit_rule),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier,
            onClick = {
                val radius = radiusText.toDoubleOrNull()
                if (radius != null && !radiusError) {
                    onSave(name, radius, location)
                } else {
                    radiusError = true
                }
            },
            enabled = name.isNotBlank() && !radiusError,
        ) {
            Text(
                text = stringResource(R.string.save_changes),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditLocPreview() {
    val loc =
        Location(
            id = 1,
            name = "loc",
            latitude = 2.0,
            longitude = 2.0,
            radius = 50.0,
        )

    EditLocView(
        location = loc,
        onSave = { _, _, _ -> },
        onNewCenter = {},
    )
}
