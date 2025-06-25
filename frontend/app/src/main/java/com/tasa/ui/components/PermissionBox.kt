package com.tasa.ui.components

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState

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
    onGranted: @Composable BoxScope.(List<String>) -> Unit,
) {
    val context = LocalContext.current
    var errorText by remember {
        mutableStateOf("")
    }
    var showBackgroundLocationDialog by remember {
        mutableStateOf(true)
    }

    val permissionState =
        rememberMultiplePermissionsState(permissions = permissions) { map ->
            val rejectedPermissions = map.filterValues { !it }.keys
            errorText =
                if (rejectedPermissions.none { it in requiredPermissions }) {
                    ""
                } else {
                    "${rejectedPermissions.joinToString()} required for the sample"
                }
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
                description,
                errorText,
            )
        }
        // FOR DND
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }
        // FOR BACKGROUND LOCATION

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                BackgroundLocationPermissionDialog(
                    show = showBackgroundLocationDialog,
                    onDismiss = {
                        showBackgroundLocationDialog = false
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                context as androidx.activity.ComponentActivity,
                                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                0,
                            )
                        }
                    },
                    context = context,
                )
            }
        }
    }
}

@Composable
fun BackgroundLocationPermissionDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    context: Context,
) {
    if (!show) return

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val intent =
                    Intent(ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                context.startActivity(intent)
                onDismiss()
            }) {
                Text("Permitir Sempre")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        title = {
            Text(
                text = "Permissão em Segundo Plano",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            )
        },
        text = {
            Text(
                text =
                    "Para que a TASA funcione corretamente enquanto está em segundo plano, " +
                        "é necessário conceder permissão de localização \"Sempre permitir\" nas definições.",
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

/**
 * The PermissionBox uses a [Box] to show a simple permission request UI when the provided [permission]
 * is revoked or the provided [onGranted] content if the permission is granted.
 *
 * This composable follows the permission request flow but for a complete example check the samples
 * under privacy/permissions
 */
@Composable
fun PermissionBox(
    modifier: Modifier = Modifier,
    permission: String,
    description: String? = null,
    contentAlignment: Alignment = Alignment.TopStart,
    onGranted: @Composable BoxScope.() -> Unit,
) {
    PermissionBox(
        modifier,
        permissions = listOf(permission),
        requiredPermissions = listOf(permission),
        description,
        contentAlignment,
    ) { onGranted() }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PermissionBoxPreview() {
    PermissionBox(
        permissions = listOf("android.permission.ACCESS_FINE_LOCATION"),
        requiredPermissions = listOf("android.permission.ACCESS_FINE_LOCATION"),
        description = "This is a sample description for the permissions required.",
    ) {
        Box(modifier = Modifier.Companion.fillMaxSize()) {
            // Content to show when permission is granted
        }
    }
}
