package com.tasa.ui.components

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.tasa.R
import androidx.core.net.toUri

/**
 * A variation of [PermissionBox] that takes a list of permissions and only calls [onGranted] when
 * all the [requiredPermissions] are granted.
 *
 * By default it assumes that all [permissions] are required.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionBox(
    modifier: Modifier = Modifier,
    permissions: List<String>,
    requiredPermissions: List<String> = permissions,
    description: String? = null,
    contentAlignment: Alignment = Alignment.Companion.TopStart,
    onSentToSettings: () -> Unit,
    onDenied: () -> Unit,
    onGranted: @Composable BoxScope.(List<String>) -> Unit,
) {
    var errorText by remember {
        mutableStateOf("")
    }
    val permissionState =
        rememberMultiplePermissionsState(permissions = permissions) { map ->
            val rejectedPermissions = map.filterValues { !it }.keys
        }
    val allRequiredPermissionsGranted =
        permissionState.revokedPermissions.none { it.permission in requiredPermissions }

    Box(
        modifier =
            Modifier.Companion
                .fillMaxSize()
                .then(modifier),
        contentAlignment =
            if (allRequiredPermissionsGranted) {
                contentAlignment
            } else {
                Alignment.Companion.Center
            },
    ) {
        if (allRequiredPermissionsGranted) {
            onGranted(
                permissionState.permissions
                    .filter { it.status.isGranted }
                    .map { it.permission },
            )
        } else {
            PermissionScreen(
                permissionState,
                onSentToSettings = onSentToSettings,
                onDenied = {
                    onDenied()
                },
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SpecialPermissionsHandler(
    modifier: Modifier = Modifier,
    onRejected: () -> Unit,
    onAllGranted: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var showBackgroundLocationDialog by remember { mutableStateOf(false) }
    var showDndDialog by remember { mutableStateOf(false) }
    val backgroundPermission =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            rememberPermissionState(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        } else {
            null
        }
    val notificationManager =
        remember {
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
    var isDndGranted by remember { mutableStateOf(notificationManager.isNotificationPolicyAccessGranted) }
    val checkPermissions = {
        isDndGranted = notificationManager.isNotificationPolicyAccessGranted
    }
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    checkPermissions()
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val allSpecialPermissionsGranted =
        remember(isDndGranted, backgroundPermission?.status) {
            val backgroundGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundPermission?.status?.isGranted == true
                } else {
                    true
                }

            backgroundGranted && isDndGranted
        }
    val needsBackgroundLocation =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            backgroundPermission?.status?.isGranted != true
        } else {
            false
        }
    LaunchedEffect(backgroundPermission?.status, isDndGranted) {
        when {
            needsBackgroundLocation -> {
                showBackgroundLocationDialog = true
                showDndDialog = false
            }
            !isDndGranted -> {
                showBackgroundLocationDialog = false
                showDndDialog = true
            }
            else -> {
                showBackgroundLocationDialog = false
                showDndDialog = false
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            // 1. Diálogo de Background Location
            showBackgroundLocationDialog -> {
                BackgroundLocationPermissionDialog(
                    onGrant = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            backgroundPermission?.launchPermissionRequest()
                        }
                        val intent =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        context.startActivity(intent)
                    },
                    onDismiss = {
                        showBackgroundLocationDialog = false
                        onRejected()
                    },
                )
            }

            // 2. Diálogo de DND
            showDndDialog -> {
                DndPermissionDialog(
                    onGrant = {
                        // Abrir configurações específicas de DND
                        val intent =
                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        context.startActivity(intent)
                    },
                    onDismiss = {
                        showDndDialog = false
                        // checkPermissions()
                        onRejected()
                    },
                )
            }
            allSpecialPermissionsGranted -> {
                onAllGranted()
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun BackgroundLocationPermissionDialog(
    onGrant: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onGrant) {
                Text(text = stringResource(R.string.allow_always))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.background_permission),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.background_permission_description),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            )
        },
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun DndPermissionDialog(
    onGrant: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = onGrant) {
                Text(text = stringResource(R.string.allow))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        title = {
            Text(
                text = stringResource(R.string.allow_access_to_dnd),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        },
        text = {
            Text(
                text = stringResource(R.string.allow_access_to_dnd_description),
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            )
        },
        tonalElevation = 6.dp,
        shape = MaterialTheme.shapes.large,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
