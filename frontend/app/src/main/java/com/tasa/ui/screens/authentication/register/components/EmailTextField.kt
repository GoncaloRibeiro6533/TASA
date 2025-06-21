package com.tasa.ui.screens.authentication.register.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tasa.ui.screens.authentication.components.AuthenticationTextField
import com.tasa.ui.screens.authentication.components.validateEmail

@Composable
fun EmailTextField(
    email: String,
    onEmailChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invalidEmailMessage = "Invalid email"
    val invalidEmail = email.isNotEmpty() && !validateEmail(email)

    AuthenticationTextField(
        label = "Email",
        value = email,
        onValueChange = onEmailChangeCallback,
        modifier = Modifier.fillMaxWidth().then(modifier),
        required = true,
        errorMessage = if (invalidEmail) invalidEmailMessage else null,
    )
}
