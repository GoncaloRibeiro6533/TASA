package com.tasa.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
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

            FloatingActionButton(
                modifier =
                    Modifier.Companion
                        .align(Alignment.Companion.BottomEnd)
                        .padding(16.dp),
                onClick = {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            data = "package:${context.packageName}".toUri()
                        }
                    context.startActivity(intent)
                },
            ) {
                Icon(imageVector = Icons.Rounded.Settings, contentDescription = "App settings")
            }
        }
    }
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