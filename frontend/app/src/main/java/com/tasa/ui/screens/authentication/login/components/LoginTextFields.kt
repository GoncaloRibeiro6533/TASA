package com.tasa.ui.screens.authentication.login.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.ui.screens.authentication.components.PasswordTextField
import com.tasa.ui.screens.authentication.components.UsernameTextField

private const val USERNAME_TO_PASSWORD_PADDING = 8
private const val TEXT_FIELD_WIDTH_FACTOR = 0.6f

const val LOGIN_USERNAME_TEXT_FIELD = "login_username_text_fields"
const val LOGIN_PASSWORD_TEXT_FIELD = "login_password_text_fields"

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
            modifier = Modifier
                .padding(bottom = USERNAME_TO_PASSWORD_PADDING.dp)
                .testTag(LOGIN_USERNAME_TEXT_FIELD),
        )
        PasswordTextField(
            password = password,
            onPasswordChangeCallback = onPasswordChangeCallback,
            modifier = Modifier.testTag(LOGIN_PASSWORD_TEXT_FIELD),
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
