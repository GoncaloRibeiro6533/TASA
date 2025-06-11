package com.tasa.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    state: MultiplePermissionsState,
    description: String? = null,
    errorText: String = "",
) {
    var showRationale by remember(state) { mutableStateOf(false) }

    val permissions =
        remember(state.revokedPermissions) {
            state.revokedPermissions.joinToString(separator = "\n") {
                "• " + it.permission.removePrefix("android.permission.")
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Permissões Necessárias",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Esta funcionalidade precisa das seguintes permissões:",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = permissions,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )

        description?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (state.shouldShowRationale) {
                    showRationale = true
                } else {
                    state.launchMultiplePermissionRequest()
                }
            },
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Conceder Permissões")
        }

        if (errorText.isNotBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (showRationale) {
        PermissionRationaleDialog(
            permissions = permissions,
            onDismiss = { showRationale = false },
            onConfirm = {
                showRationale = false
                state.launchMultiplePermissionRequest()
            }
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
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text("Permissões Requeridas")
        },
        text = {
            Text(
                "Para continuar, é necessário conceder as seguintes permissões:\n\n$permissions"
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
        }
    )
}
