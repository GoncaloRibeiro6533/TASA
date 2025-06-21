package com.tasa.ui.screens.authentication.login.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tasa.ui.screens.authentication.components.IconButton

private const val BUTTON_PADDING = 16

/**
 * Button for login operation.
 *
 * @param enabled whether the button is enabled or not
 * @param onLoginClickCallback callback to be invoked when the login button is clicked
 */
@Composable
fun LoginButton(
    enabled: Boolean = true,
    onLoginClickCallback: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        onClick = onLoginClickCallback,
        enabled = enabled,
        modifier = modifier.padding(BUTTON_PADDING.dp),
        text = "Login",
        painter = painterResource(id = com.tasa.R.drawable.ic_round_login_24),
        contentDescription = "LoginButton",
    )
}
