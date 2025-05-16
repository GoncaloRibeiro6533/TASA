package com.tasa.authentication.register.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.authentication.components.PasswordTextField
import com.tasa.authentication.components.UsernameTextField

private const val EMAIL_TO_USERNAME_PADDING = 8
private const val USERNAME_TO_PASSWORD_PADDING = 8
private const val TEXT_FIELD_WIDTH_FACTOR = 0.6f

@Composable
fun RegisterTextFields(
    email: String,
    username: String,
    password: String,
    onEmailChangeCallback: (String) -> Unit,
    onUsernameChangeCallback: (String) -> Unit,
    onPasswordChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(TEXT_FIELD_WIDTH_FACTOR)) {
        EmailTextField(
            email = email,
            onEmailChangeCallback = onEmailChangeCallback,
            modifier = Modifier.padding(bottom = EMAIL_TO_USERNAME_PADDING.dp).fillMaxWidth(),
        )

        UsernameTextField(
            username = username,
            onUsernameChangeCallback = onUsernameChangeCallback,
            modifier = Modifier.padding(bottom = USERNAME_TO_PASSWORD_PADDING.dp).fillMaxWidth(),
        )

        PasswordTextField(
            password = password,
            onPasswordChangeCallback = onPasswordChangeCallback,
            modifier = Modifier,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterTextFieldsPreview() {
    RegisterTextFields("", "", "", {}, {}, {})
}
