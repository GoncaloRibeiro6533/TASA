package com.tasa.ui.screens.authentication.register.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tasa.R
import com.tasa.ui.screens.authentication.components.AuthenticationTextField
import com.tasa.ui.screens.authentication.components.validateEmail

@Composable
fun EmailTextField(
    email: String,
    onEmailChangeCallback: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invalidEmailMessage = stringResource(R.string.invalid_email)
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
