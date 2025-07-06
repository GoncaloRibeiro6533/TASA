package com.tasa.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.tasa.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    state: MultiplePermissionsState,
    onSentToSettings: () -> Unit,
    onDenied: () -> Unit,
) {
    var hasRequested by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    LaunchedEffect(state, state.revokedPermissions) {
        if (!hasRequested) {
            hasRequested = true
            state.launchMultiplePermissionRequest()
        }
        if (state.revokedPermissions.isNotEmpty()) {
            showDialog = true
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        if (showDialog && state.revokedPermissions.isNotEmpty()) {
            PermissionRationaleDialog(
                permissions = state.revokedPermissions.joinToString { it.permission },
                onDismiss = {
                    onDenied()
                },
                onConfirm = {
                    if (state.shouldShowRationale) {
                        state.launchMultiplePermissionRequest()
                    } else {
                        onSentToSettings()
                        hasRequested = true
                    }
                },
            )
        }
    }
}

@Composable
fun PermissionRationaleDialog(
    permissions: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(stringResource(R.string.necessary_permissions))
        },
        text = {
            Text(
                stringResource(R.string.necessary_permissions_description),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(R.string.Ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    val dummyState =
        object : MultiplePermissionsState {
            override val permissions: List<PermissionState> = emptyList<PermissionState>()
            override val revokedPermissions: List<PermissionState>
                get() =
                    {
                        emptyList<PermissionState>()
                    }()
            override val shouldShowRationale = false
            override val allPermissionsGranted = false

            override fun launchMultiplePermissionRequest() {}
        }
    PermissionScreen(
        state = dummyState,
        onSentToSettings = { /* Handle sent to settings */ },
        onDenied = { /* Handle denied permissions */ },
    )
}
