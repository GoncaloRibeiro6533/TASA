package com.tasa.newevent.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.tasa.R
import com.tasa.ui.screens.authentication.components.IconButton

private const val BUTTON_PADDING = 8

@Composable
fun AddEventButton(
    enabled: Boolean = true,
    modifier: Modifier,
    onRegisterClickCallback: () -> Unit,
) {
    IconButton(
        onClick = onRegisterClickCallback,
        enabled = enabled,
        modifier = modifier.padding(BUTTON_PADDING.dp),
        text = "AddEvent",
        painter = painterResource(id = R.drawable.ic_round_login_24),
        contentDescription = "Button to register screen",
    )
}
