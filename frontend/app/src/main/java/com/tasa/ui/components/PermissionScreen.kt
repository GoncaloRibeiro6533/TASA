package com.tasa.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.tasa.R

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    state: MultiplePermissionsState,
    description: String? = null,
    errorText: String = "",
) {
    var hasRequested by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (!hasRequested) {
            hasRequested = true
            state.launchMultiplePermissionRequest()
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        Image(
            painter = painterResource(id = R.drawable.tasa_logo),
            contentDescription = "Tasa logo",
            modifier = Modifier.padding(10.dp).size(250.dp),
            alignment = Alignment.Center,
        )
    }
}

@Composable
fun PermissionRationaleDialog(
    permissions: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text("Permissões Requeridas")
        },
        text = {
            Text(
                "Para continuar, é necessário conceder as seguintes permissões:\n\n$permissions",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Continuar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
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
                get() = {
                    emptyList<PermissionState>()
                }()
            override val shouldShowRationale = false
            override val allPermissionsGranted = false

            override fun launchMultiplePermissionRequest() {}
        }
    PermissionScreen(
        state = dummyState,
        description = "This is a sample permission screen",
        errorText = "Some permissions are required",
    )
}
