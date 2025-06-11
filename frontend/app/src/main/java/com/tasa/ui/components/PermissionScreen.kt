package com.tasa.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    state: MultiplePermissionsState,
    description: String?,
    errorText: String,
) {
    var showRationale by remember(state) {
        mutableStateOf(false)
    }

    val permissions =
        remember(state.revokedPermissions) {
            state.revokedPermissions.joinToString("\n") {
                " - " + it.permission.removePrefix("android.permission.")
            }
        }
    Column(
        modifier =
            Modifier.Companion
                .fillMaxWidth()
                .animateContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Text(
            text = "Sample requires permission/s:",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.Companion.padding(16.dp),
        )
        Text(
            text = permissions,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.Companion.padding(16.dp),
        )
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.Companion.padding(16.dp),
            )
        }
        Button(
            onClick = {
                if (state.shouldShowRationale) {
                    showRationale = true
                } else {
                    state.launchMultiplePermissionRequest()
                }
            },
        ) {
            Text(text = "Grant permissions")
        }
        if (errorText.isNotBlank()) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.Companion.padding(16.dp),
            )
        }
    }
    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
            },
            title = {
                Text(text = "Permissions required by the sample")
            },
            text = {
                Text(text = "The sample requires the following permissions to work:\n $permissions")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        state.launchMultiplePermissionRequest()
                    },
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                    },
                ) {
                    Text("Dismiss")
                }
            },
        )
    }
}
