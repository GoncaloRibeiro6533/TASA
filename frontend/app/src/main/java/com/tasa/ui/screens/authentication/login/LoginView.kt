package com.tasa.ui.screens.authentication.login

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tasa.R
import com.tasa.ui.screens.authentication.components.validatePassword
import com.tasa.ui.screens.authentication.components.validateUsername
import com.tasa.ui.screens.authentication.login.components.LoginButton
import com.tasa.ui.screens.authentication.login.components.LoginTextFields
import com.tasa.ui.theme.TasaTheme

const val LOGIN_VIEW = "login_view"
const val LOGIN_TEXT_FIELDS = "login_text_fields"
const val LOGIN_BUTTON = "login_button"
const val REGISTER_ANCHOR = "register_anchor"

@Composable
fun LoginView(
    onSubmit: (String, String) -> Unit,
    onRegisterRequested: () -> Unit,
) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val invalidFields =
        (username.isEmpty() || password.isEmpty()) ||
            username.isNotEmpty() && !validateUsername(username) ||
            password.isNotEmpty() && !validatePassword(password)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .testTag(LOGIN_VIEW),
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.titleLarge,
            )
        }
        LoginTextFields(
            username = username,
            password = password,
            onUsernameChangeCallback = { username = it },
            onPasswordChangeCallback = { password = it },
            modifier = Modifier.testTag(LOGIN_TEXT_FIELDS),
        )
        LoginButton(
            enabled = !invalidFields,
            modifier = Modifier.testTag(LOGIN_BUTTON),
            onLoginClickCallback = {
                onSubmit(username, password)
            },
        )

        val annotatedString =
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(stringResource(R.string.register))
                }
            }
        Row(
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(text = stringResource(R.string.dont_have_a_account), style = TextStyle(fontSize = 18.sp))
            Text(
                text = annotatedString,
                style = TextStyle(fontSize = 18.sp, color = MaterialTheme.colorScheme.primary),
                modifier =
                    Modifier
                        .clickable { onRegisterRequested() }
                        .testTag(REGISTER_ANCHOR),
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PreviewLoginView() {
    TasaTheme {
        LoginView(
            onSubmit = { _, _ -> },
            onRegisterRequested = { },
        )
    }
}
