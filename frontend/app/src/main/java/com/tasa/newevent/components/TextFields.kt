package com.tasa.newevent.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation


@Suppress("Deprecated")
@Composable
fun NewEventTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    maxLength: Int? = null,
    forbiddenCharacters: List<Char> = listOf<Char>(' ', '\n', '\t'),
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    TextField(
        label = {
            Text(
                text = "$label${if (required) " *" else ""}" +
                        if (errorMessage != null) " - $errorMessage" else ""
            )
        },
        value = value,
        onValueChange = {
            var filteredValue = it.filter {
                    c ->
                c !in forbiddenCharacters
            }
            if (maxLength != null && filteredValue.length > maxLength)
                filteredValue = filteredValue.substring(0 until maxLength)

            onValueChange(filteredValue)
        },
        singleLine = true,
        modifier = modifier,
        isError = errorMessage != null,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)

    )
}