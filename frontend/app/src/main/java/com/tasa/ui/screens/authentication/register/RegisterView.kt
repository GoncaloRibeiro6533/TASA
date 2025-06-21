package com.tasa.ui.screens.authentication.register

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tasa.ui.screens.authentication.components.validateEmail
import com.tasa.ui.screens.authentication.components.validatePassword
import com.tasa.ui.screens.authentication.components.validateUsername
import com.tasa.ui.screens.authentication.register.components.RegisterButton
import com.tasa.ui.screens.authentication.register.components.RegisterTextFields

@Composable
fun RegisterView(onSubmit: (String, String, String) -> Unit) {
    val orientation = LocalConfiguration.current.orientation
    var email by rememberSaveable { mutableStateOf("") }
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val invalidFields =
        email.isEmpty() || username.isEmpty() || password.isEmpty() ||
            (email.isNotEmpty() && !validateEmail(email)) ||
            (username.isNotEmpty() && !validateUsername(username)) ||
            (password.isNotEmpty() && !validatePassword(password))

    // Layout para portrait
    PortraitRegisterLayout(
        email = email,
        username = username,
        password = password,
        onEmailChange = { email = it.trim() },
        onUsernameChange = { username = it.trim() },
        onPasswordChange = { password = it.trim() },
        invalidFields = invalidFields,
        onSubmit = onSubmit,
    )
}

@Composable
fun PortraitRegisterLayout(
    email: String,
    username: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    invalidFields: Boolean,
    onSubmit: (String, String, String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
    ) {
        Text(
            text = "Register",
            modifier = Modifier.padding(bottom = 16.dp),
        )

        RegisterTextFields(
            email = email,
            username = username,
            password = password,
            onEmailChangeCallback = onEmailChange,
            onUsernameChangeCallback = onUsernameChange,
            onPasswordChangeCallback = onPasswordChange,
            modifier = Modifier,
        )

        RegisterButton(
            enabled = !invalidFields,
            modifier = Modifier,
        ) {
            onSubmit(username, password, email)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    RegisterView { _, _, _ -> }
}
