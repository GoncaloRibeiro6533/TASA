package com.tasa.ui.screens.authentication.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun AuthenticationTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    maxLength: Int? = null,
    forbiddenCharacters: List<Char> = listOf<Char>(' ', '\n', '\t'),
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    TextField(
        label = {
            Text(
                text =
                    "$label${if (required) " *" else ""}" +
                        if (errorMessage != null) " - $errorMessage" else "",
            )
        },
        value = value,
        onValueChange = {
            var filteredValue =
                it.filter {
                        c ->
                    c !in forbiddenCharacters
                }
            if (maxLength != null && filteredValue.length > maxLength) {
                filteredValue = filteredValue.substring(0 until maxLength)
            }

            onValueChange(filteredValue)
        },
        singleLine = true,
        modifier = modifier,
        isError = errorMessage != null,
        visualTransformation = visualTransformation,
        keyboardOptions =
            if (label == "Password") {
                KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    autoCorrect = false,
                )
            } else {
                KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                )
            },
    )
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun AuthenticationTextFieldPreview() {
    AuthenticationTextField(
        label = "Username",
        value = "test_user",
        onValueChange = {},
        modifier = Modifier,
        required = true,
        maxLength = 20,
        forbiddenCharacters = listOf(' ', '\n', '\t'),
        errorMessage = null,
    )
}
