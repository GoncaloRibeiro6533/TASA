package com.tasa.ui.screens.authentication.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

private const val BUTTON_PADDING = 6

@Composable
fun IconButton(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    text: String? = null,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        enabled = enabled,
        contentPadding = if (text == null) PaddingValues(0.dp) else ButtonDefaults.ContentPadding,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            modifier = iconModifier,
        )

        if (text != null) {
            Spacer(modifier = Modifier.width(BUTTON_PADDING.dp))
            Text(text = text)
        }
    }
}
