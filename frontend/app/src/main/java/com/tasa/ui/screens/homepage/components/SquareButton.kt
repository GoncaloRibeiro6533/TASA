package com.tasa.ui.screens.homepage.components

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun SquareButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(containerColor = Color.Companion.LightGray),
    shape: Shape = MaterialTheme.shapes.medium,
) {
    Button(
        onClick = onClick,
        modifier =
            modifier
                .aspectRatio(1f)
                .padding(4.dp),
        colors = colors,
        shape = shape,
    ) {
        TextBox(label)
    }
}
