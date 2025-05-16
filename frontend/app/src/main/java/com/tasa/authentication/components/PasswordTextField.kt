package com.tasa.authentication.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun PasswordTextField(
    password: String,
    onPasswordChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invalidPasswordMessage = "Password should have at least 8 characters."
    val invalidPassword = password.isNotEmpty() && !validatePassword(password)

    AuthenticationTextField(
        label = "Password",
        value = password,
        onValueChange = onPasswordChangeCallback,
        visualTransformation = PasswordVisualTransformation(),
        modifier = modifier.fillMaxWidth(),
        required = true,
        maxLength = MAX_PASSWORD_LENGTH,
        errorMessage = if (invalidPassword) invalidPasswordMessage else null,
    )
}
