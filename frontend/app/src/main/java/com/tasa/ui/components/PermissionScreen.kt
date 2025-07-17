package com.tasa.ui.components

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.tasa.R

@Composable
fun PermissionScreen(
    permissions: List<String>,
    onAllGranted: () -> Unit,
    onSentToSettings: () -> Unit,
    onDenied: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity

    var showDialog by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { result ->
            val denied = result.filterValues { !it }.keys

            if (denied.isEmpty()) {
                onAllGranted()
            } else {
                // Verifica se alguma foi marcada como "não perguntar novamente"
                permanentlyDenied =
                    denied.any {
                        !ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
                    }
                showDialog = true
            }
        }

    LaunchedEffect(Unit) {
        launcher.launch(permissions.toTypedArray())
    }

    if (showDialog) {
        PermissionRationaleDialog(
            onDismiss = {
                showDialog = false
                onDenied()
            },
            onConfirm = {
                showDialog = false
                if (permanentlyDenied) {
                    onSentToSettings()
                } else {
                    launcher.launch(permissions.toTypedArray())
                }
            },
        )
    }
}

@Composable
fun PermissionRationaleDialog(
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
    PermissionScreen(
        onSentToSettings = { /* Handle sent to settings */ },
        onDenied = { /* Handle denied permissions */ },
        permissions = listOf(),
        onAllGranted = {},
    )
}
