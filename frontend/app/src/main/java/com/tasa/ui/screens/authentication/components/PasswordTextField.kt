package com.tasa.ui.screens.authentication.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.tasa.R

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invalidPasswordMessage = stringResource(R.string.invalid_password)
    val invalidPassword = password.isNotEmpty() && !validatePassword(password)

    AuthenticationTextField(
        label = stringResource(R.string.password_label),
        value = password,
        onValueChange = onPasswordChangeCallback,
        visualTransformation = PasswordVisualTransformation(),
        modifier = modifier.fillMaxWidth(),
        required = true,
        maxLength = MAX_PASSWORD_LENGTH,
        errorMessage = if (invalidPassword) invalidPasswordMessage else null,
    )
}
