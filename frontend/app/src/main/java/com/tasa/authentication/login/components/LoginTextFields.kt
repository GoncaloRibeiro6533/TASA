package com.tasa.authentication.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.authentication.components.PasswordTextField
import com.tasa.authentication.components.UsernameTextField

private const val USERNAME_TO_PASSWORD_PADDING = 8
private const val TEXT_FIELD_WIDTH_FACTOR = 0.6f

@Composable
fun LoginTextFields(
    username: String,
    password: String,
    onUsernameChangeCallback: (String) -> Unit,
    onPasswordChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(TEXT_FIELD_WIDTH_FACTOR)) {
        UsernameTextField(
            username = username,
            onUsernameChangeCallback = onUsernameChangeCallback,
            modifier = Modifier.padding(bottom = USERNAME_TO_PASSWORD_PADDING.dp),
        )
        PasswordTextField(
            password = password,
            onPasswordChangeCallback = onPasswordChangeCallback,
            modifier = Modifier,
        )
    }
}

@Preview
@Composable
fun LoginTextFieldsPreview() {
    LoginTextFields(
        username = "Bob",
        password = "password_of_bob",
        onUsernameChangeCallback = {},
        onPasswordChangeCallback = {},
    )
}
