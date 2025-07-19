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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.domain.Location

const val EDIT_LOC_VIEW = "edit_loc_view"
const val EDIT_LOC_NAME_TEXT_FIELD = "edit_loc_name_text_fields"
const val EDIT_LOC_RADIUS_TEXT_FIELD = "edit_loc_radius_fields"
const val EDIT_LOC_CENTER_BUTTON = "center_button"
const val EDIT_LOC_RULE_BUTTON = "rule_button"
const val EDIT_LOC_SAVE_BUTTON = "save_button"

@Composable
fun EditLocView(
    location: Location,
    onSave: (
        location: Location,
        name: String,
        radius: Double,
    ) -> Unit,
    onNewCenter: () -> Unit,
    onAddRule: () -> Unit,
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
                .fillMaxWidth()
                .testTag(EDIT_LOC_VIEW),
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EDIT_LOC_NAME_TEXT_FIELD),
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
            modifier = Modifier
                .fillMaxWidth()
                .testTag(EDIT_LOC_RADIUS_TEXT_FIELD),
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
                        .padding(end = 4.dp)
                        .testTag(EDIT_LOC_CENTER_BUTTON),
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
                        .padding(start = 4.dp)
                        .testTag(EDIT_LOC_RULE_BUTTON),
                onClick = {
                    onAddRule()
                },
            ) {
                Text(
                    text = stringResource(R.string.add_rule),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier
                .testTag(EDIT_LOC_SAVE_BUTTON),
            onClick = {
                val radius = radiusText.toDoubleOrNull()
                if (radius != null && !radiusError) {
                    onSave(location, name, radius)
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
        onAddRule = {},
    )
}
